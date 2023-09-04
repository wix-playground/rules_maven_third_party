package com.wix.build.sync

import com.wix.build.maven.Coordinates
import com.wix.build.maven.mapper.Mapper
import org.slf4j.LoggerFactory

import java.io.FileInputStream
import java.nio.file.{Files, Paths}
import scala.collection.{Map, concurrent}
import scala.util.Properties

trait ArtifactsChecksumCache {
  def getChecksum(artifact: Coordinates): Option[ArtifactChecksum]

  def setChecksum(artifact: Coordinates, checksum: ArtifactChecksum): Unit

  def flush(): Unit
}

trait ArtifactChecksum

case object NoChecksum extends ArtifactChecksum

case class Checksum(sum: String) extends ArtifactChecksum

class ArtifactsChecksumFileCache(fileAccessor: ArtifactsChecksumCacheFileAccessor) extends ArtifactsChecksumCache {
  private val artifactsChecksums = loadCachedChecksums()

  override def getChecksum(artifact: Coordinates): Option[ArtifactChecksum] = {
    artifactsChecksums
      .get(artifact.serialized)
      .map(sum => if (sum == "n/a") NoChecksum else Checksum(sum))
  }

  override def setChecksum(artifact: Coordinates, checksum: ArtifactChecksum): Unit = checksum match {
    case NoChecksum =>
      artifactsChecksums.update(artifact.serialized, "n/a")
    case Checksum(sum) =>
      artifactsChecksums.update(artifact.serialized, sum)
  }


  override def flush(): Unit = fileAccessor.storeChecksums(artifactsChecksums)

  private def loadCachedChecksums(): concurrent.Map[String, String] =
    concurrent.TrieMap(fileAccessor.readChecksums().toList: _*)

}

class ArtifactsChecksumCacheFileAccessor {
  private val log = LoggerFactory.getLogger(getClass)
  private val mapper = Mapper.mapper
  private val cacheFile = Paths.get(Properties.userHome, ".cache", "artifact_checksums.json").toFile

  def readChecksums(): Map[String, String] = {
    try {
      val inputStream = new FileInputStream(cacheFile)
      mapper.readValue(inputStream, classOf[Map[String, String]])
    } catch {
      case _: Exception =>
        log.warn(s"Cache file does not exist or is incorrectly formatted, file location: ${cacheFile}")
        Map[String, String]()
    }
  }

  def storeChecksums(checksums: Map[String, String]): Unit = {
    val serializedContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(checksums)

    if (!cacheFile.exists()) {
      cacheFile.getParentFile.mkdirs()
    }

    Files.writeString(cacheFile.toPath, serializedContent)
  }
}

class VoidArtifactsShaCache extends ArtifactsChecksumCache {
  override def getChecksum(artifact: Coordinates): Option[ArtifactChecksum] = None

  override def setChecksum(artifact: Coordinates, checksum: ArtifactChecksum): Unit = {}

  override def flush(): Unit = {}
}
