package com.wix.build.sync

import com.wix.build.maven._
import com.wix.build.sync.ArtifactoryRemoteStorage.{Sha256, _}
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import scalaj.http.{BaseHttp, HttpOptions, HttpResponse}

import scala.util.{Failure, Success, Try}

class MavenRepoRemoteStorage(baseUrls: List[String], cache: ArtifactsChecksumCache) extends DependenciesRemoteStorage {

  private val log = LoggerFactory.getLogger(getClass)

  private def httpClient = new BaseHttp(options = Seq(
    HttpOptions.connTimeout(10000),
    HttpOptions.readTimeout(50000),
    HttpOptions.followRedirects(false) // don't redirect (SNAPSHOTs case) to avoid calculating unstable checksum
  ))

  override def checksumFor(node: DependencyNode): Option[String] = {
    val artifact = node.baseDependency.coordinates

    getChecksum(artifact)
      .orElse(getChecksumFromCache(artifact))
      .orElse {
        log.warn("Fallback to calculating checksum by downloading the bytes...")
        calculateChecksum(artifact)
      }.toOption
  }

  private def getChecksumFromCache(artifact: Coordinates): Try[String] =
    cache.getChecksum(artifact) match {
      case Some(value) => Success(value)
      case None => throw ArtifactNotFoundException(s"Cache does not have checksum for artifact ${artifact.serialized}")
    }

  private def getChecksum(coordinates: Coordinates): Try[Sha256] = getWithFallback(getArtifactSha256, coordinates)

  private def calculateChecksum(coordinates: Coordinates): Try[Sha256] =
    for {
      res <- getWithFallback(getArtifactBytes, coordinates)
      sha256 <- calculateSha256(res)
    } yield {
      cache.setChecksum(coordinates, sha256)
      sha256
    }

  private def getWithFallback[T](getter: (Coordinates, String) => Try[T],
                                 coordinates: Coordinates): Try[T] = {
    val attempts = baseUrls.toStream.map(url => getter(coordinates, url))

    Try {
      attempts.collectFirst {
        case Success(e) => e
      }.getOrElse {
        val failures = attempts.toList.collect { case Failure(t) => t }
        val failuresAsStr = failures.map(_.getMessage).mkString("\n")
        val message = s"Failed to fetch resource\n$failuresAsStr"
        log.warn(message)
        throw new RuntimeException(message)
      }
    }
  }

  private def getArtifactSha256(artifact: Coordinates, baseUrl: String): Try[Sha256] = {
    val url = s"$baseUrl/${artifact.toSha256Path}"
    extract(response = httpClient(url).asString, inUrl = url)
  }

  private def getArtifactBytes(artifact: Coordinates, baseUrl: String): Try[Array[Byte]] = {
    val url = s"$baseUrl/${artifact.toArtifactPath}"
    extract(response = httpClient(url).asBytes, inUrl = url)
  }

  private def extract[T](response: HttpResponse[T], inUrl: String): Try[T] =
    response match {
      case r if r.isSuccess => Success(r.body)
      case r if r.is4xx => Failure(artifactNotFoundException(inUrl))
      // TODO: should probably sleep and retry in case of response code 5xx
      case r => Failure(errorFetchingArtifactException(inUrl, r))
    }

  private def calculateSha256(jar: Array[Byte]) = {
    Try {
      DigestUtils.sha256Hex(jar)
    }
  }

  private def artifactNotFoundException(url: String) =
    ArtifactNotFoundException(s"Got 'NotFound' from $url")

  private def errorFetchingArtifactException[T](url: String, r: HttpResponse[T]) =
    ErrorFetchingArtifactException(s"Got Error [${r.code} : ${r.statusLine}] from $url")
}
