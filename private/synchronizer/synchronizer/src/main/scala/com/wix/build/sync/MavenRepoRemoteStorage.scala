package com.wix.build.sync

import com.wix.build.maven._
import com.wix.build.sync.ArtifactoryRemoteStorage._
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory

import java.net.{HttpURLConnection, URI}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpClient.Redirect
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class MavenRepoRemoteStorage(baseUrls: List[String], cache: ArtifactsChecksumCache) extends DependenciesRemoteStorage {

  private val log = LoggerFactory.getLogger(getClass)

  private val httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(2))
    .followRedirects(Redirect.ALWAYS)
    .build()

  override def checksumFor(node: DependencyNode): Option[String] = {
    val artifact = node.baseDependency.coordinates

    getChecksumFromCache(artifact)
      .orElse(getChecksum(artifact))
      .orElse {
        log.warn("Fallback to calculating checksum by downloading the bytes...")
        calculateChecksum(artifact)
      }
      .toOption
      .collect { case Checksum(sum) => sum }
  }

  private def getChecksumFromCache(artifact: Coordinates): Try[ArtifactChecksum] =
    cache.getChecksum(artifact) match {
      case Some(value) =>
        Success(value)
      case None =>
        Failure {
          ArtifactNotFoundException(s"Cache does not have checksum for artifact ${artifact.serialized}")
        }
    }

  private def getChecksum(coordinates: Coordinates): Try[ArtifactChecksum] = {
    val checksum = getWithFallback(getArtifactSha256, coordinates)

    checksum match {
      case Success(checksum) => cache.setChecksum(coordinates, checksum)
      case Failure(_: ArtifactNotFoundException) =>
        cache.setChecksum(coordinates, NoChecksum)
      case Failure(e) =>
        log.error("Failed fetching resource - existing.", e)
        sys.exit(1)
    }

    checksum
  }

  private def calculateChecksum(coordinates: Coordinates): Try[ArtifactChecksum] = {
    for {
      res <- getWithFallback(getArtifactBytes, coordinates)
      sha256 <- calculateSha256(res)
    } yield {
      sha256
    }
  }

  def allAttemptsWereNotFound(failures: List[Throwable]): Boolean =
    failures.forall(_.isInstanceOf[ArtifactNotFoundException])

  private def getWithFallback[T](getter: (Coordinates, String) => Try[T],
                                 coordinates: Coordinates): Try[T] = {
    val attempts = baseUrls.toStream.map(url => getter(coordinates, url))

    Try {
      attempts.collectFirst {
        case Success(e) => e
      } getOrElse {
        val failures = attempts.toList.collect { case Failure(t) => t }
        val failuresAsStr = failures.map(_.getMessage).mkString("\n")
        val message = s"Failed to fetch resource\n$failuresAsStr"
        log.warn(message)

        if (allAttemptsWereNotFound(failures)) {
          throw ArtifactNotFoundException(failuresAsStr)
        }
        else {
          log.error(s"Exiting - failed connection: $failuresAsStr")
          sys.exit(1)
        }
      }
    }
  }

  private def getRequestFor(url: String) = HttpRequest
    .newBuilder(URI.create(url))
    .timeout(Duration.ofSeconds(30))
    .GET()
    .build()

  private def getArtifactSha256(artifact: Coordinates, baseUrl: String): Try[ArtifactChecksum] = {
    val url = s"$baseUrl/${artifact.toSha256Path}"
    val request = getRequestFor(url)
    val response = retry(2)(request, BodyHandlers.ofString())
    extract(response = response, inUrl = url).map(sum => Checksum(sum))
  }

  private def getArtifactBytes(artifact: Coordinates, baseUrl: String): Try[Array[Byte]] = {
    val url = s"$baseUrl/${artifact.toArtifactPath}"
    val request = getRequestFor(url)
    val response = retry(2)(request, BodyHandlers.ofByteArray())
    extract(response = response, inUrl = url)
  }

  private def extract[T](response: HttpResponse[T], inUrl: String): Try[T] =
    response match {
      case r if r.statusCode() == HttpURLConnection.HTTP_OK => Success(r.body)
      case r if r.statusCode() == HttpURLConnection.HTTP_NOT_FOUND => Failure(artifactNotFoundException(inUrl))
      case r => Failure(errorFetchingArtifactException(inUrl, r))
    }

  private def calculateSha256(jar: Array[Byte]): Try[ArtifactChecksum] = {
    Try {
      Checksum(DigestUtils.sha256Hex(jar))
    }
  }

  private def artifactNotFoundException(url: String) =
    ArtifactNotFoundException(s"Got 'NotFound' from $url")

  private def errorFetchingArtifactException[T](url: String, r: HttpResponse[T]) =
    ErrorFetchingArtifactException(s"Got Error [${r.statusCode()} : ${r.body()}] from $url")

  @tailrec
  private def retryTry[T](n: Int)(httpRequest: HttpRequest, bodyHandler: HttpResponse.BodyHandler[T]): Try[HttpResponse[T]] = {
    val response = Try(httpClient.send(httpRequest, bodyHandler))
    val sleepMillis = 1000
    response match {
      case Success(r) if r.statusCode() >= 500 && r.statusCode() < 599 =>
        log.warn(s"Retrying GET to ${httpRequest.uri()}, previously failed status ${r.statusCode()}, sleeping for $sleepMillis millis")
        Thread.sleep(sleepMillis)
        retryTry[T](n - 1)(httpRequest, bodyHandler)
      case Failure(t) =>
        log.warn(s"Retrying GET to ${httpRequest.uri()}, previously failed with ${t}, sleeping for $sleepMillis millis")
        Thread.sleep(sleepMillis)
        retryTry[T](n - 1)(httpRequest, bodyHandler)
      case _ => response
    }
  }

  private def retry[T](n: Int)(httpRequest: HttpRequest, bodyHandler: HttpResponse.BodyHandler[T]): HttpResponse[T] = {
    retryTry(n)(httpRequest, bodyHandler).get
  }
}
