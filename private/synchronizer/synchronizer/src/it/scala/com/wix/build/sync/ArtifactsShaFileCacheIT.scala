package com.wix.build.sync

import com.wix.build.maven.Coordinates

import java.util.UUID
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import org.specs2.mock.Mockito.theStubbed
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class ArtifactsShaFileCacheIT extends SpecificationWithJUnit {
  sequential

  "ArtifactsShaFileCache" should {
    "return checksum as cache already has it" in new ctx {
      fileAccessor.readChecksums() returns Map[String, String](artifact.serialized -> sha256Checksum)
      val cache = new ArtifactsChecksumFileCache(fileAccessor)

      cache.getChecksum(artifact) must beSome(sha256Checksum)
    }

    "return empty checksum when cache does not store it" in new ctx {
      fileAccessor.readChecksums() returns Map[String, String]()
      val cache = new ArtifactsChecksumFileCache(fileAccessor)

      cache.getChecksum(artifact) must beEmpty
    }

    "store checksum for an artifact and retrieve it" in new ctx {
      fileAccessor.readChecksums() returns Map[String, String]()
      val cache = new ArtifactsChecksumFileCache(fileAccessor)

      cache.setChecksum(artifact, sha256Checksum)

      cache.getChecksum(artifact) must beSome(sha256Checksum)
    }

    "store checksum should override checksum for an artifact" in new ctx {
      fileAccessor.readChecksums() returns Map[String, String]()
      val cache = new ArtifactsChecksumFileCache(fileAccessor)
      cache.setChecksum(artifact, sha256Checksum)
      val otherChecksum = UUID.randomUUID().toString

      cache.setChecksum(artifact, otherChecksum)

      cache.getChecksum(artifact) must beSome(otherChecksum)
    }
  }

  trait ctx extends Scope {
    val fileAccessor = Mockito.mock[ArtifactsChecksumCacheFileAccessor]
    val sha256Checksum = UUID.randomUUID().toString
    val artifact = Coordinates("org.specs2", "specs2-analysis_2.12", "4.3.1")
  }
}

