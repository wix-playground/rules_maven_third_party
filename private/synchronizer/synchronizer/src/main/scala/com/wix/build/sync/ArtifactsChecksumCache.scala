package com.wix.build.sync

import better.files.File
import com.wix.build.maven.Coordinates
import com.wix.build.maven.mapper.Mapper
import org.slf4j.LoggerFactory

import scala.collection.{Map, mutable}

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

  private def loadCachedChecksums(): mutable.Map[String, String] = scala.collection.mutable.Map(fileAccessor.readChecksums().toList: _*)
}

class ArtifactsChecksumCacheFileAccessor {
  private val log = LoggerFactory.getLogger(getClass)
  private val mapper = Mapper.mapper
  private val cacheFile = File.home / ".cache" / "artifact_checksums.json"

  def readChecksums(): Map[String, String] = {
    try {
      val source = cacheFile.contentAsString
      mapper.readValue(source, classOf[Map[String, String]])
    } catch {
      case _: Exception =>
        log.warn(s"Cache file does not exists or is incorrectly formatted, file location: ${cacheFile.path}")
        Map[String, String]()
    }
  }

  def storeChecksums(checksums: Map[String, String]): Unit = {
    val serializedContent = mapper.writeValueAsString(checksums)

    cacheFile
      .createIfNotExists(asDirectory = false, createParents = true)
      .overwrite(serializedContent)
  }
}

class VoidArtifactsShaCache extends ArtifactsChecksumCache {
  override def getChecksum(artifact: Coordinates): Option[String] = None

  override def setChecksum(artifact: Coordinates, checksum: String): Unit = {}

  override def flush(): Unit = {}
}
