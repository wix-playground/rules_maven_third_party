package com.wix.build.sync

import com.wix.build.maven.Coordinates
import com.wix.build.maven.mapper.Mapper
import org.slf4j.LoggerFactory

import java.io.FileInputStream
import java.nio.file.{Files, Paths}
import scala.collection.{Map, mutable}
import scala.util.Properties

trait ArtifactsChecksumCache {
  def getChecksum(artifact: Coordinates): Option[String]

  def setChecksum(artifact: Coordinates, checksum: String): Unit

  def flush(): Unit
}

class ArtifactsChecksumFileCache(fileAccessor: ArtifactsChecksumCacheFileAccessor) extends ArtifactsChecksumCache {
  private val artifactsChecksums = loadCachedChecksums()

  override def getChecksum(artifact: Coordinates): Option[String] = {
    artifactsChecksums.get(artifact.serialized)
  }

  override def setChecksum(artifact: Coordinates, checksum: String): Unit = {
    artifactsChecksums.update(artifact.serialized, checksum)
  }

  override def flush(): Unit = fileAccessor.storeChecksums(artifactsChecksums)

  private def loadCachedChecksums(): mutable.Map[String, String] = mutable.Map(fileAccessor.readChecksums().toList: _*)
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
    val serializedContent = mapper.writeValueAsString(checksums)

    if (!cacheFile.exists()) {
      cacheFile.getParentFile.mkdirs()
    }

    Files.writeString(cacheFile.toPath, serializedContent)
  }
}

class VoidArtifactsShaCache extends ArtifactsChecksumCache {
  override def getChecksum(artifact: Coordinates): Option[String] = None

  override def setChecksum(artifact: Coordinates, checksum: String): Unit = {}

  override def flush(): Unit = {}
}
