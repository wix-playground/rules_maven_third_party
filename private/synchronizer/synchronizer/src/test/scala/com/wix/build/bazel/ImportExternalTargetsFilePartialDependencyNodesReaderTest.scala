package com.wix.build.bazel

import com.wix.build.maven.MavenMakers._
import com.wix.build.maven.translation.MavenToBazelTranslations.`Maven Coordinates to Bazel rules`
import org.specs2.mutable.SpecificationWithJUnit

class ImportExternalTargetsFilePartialDependencyNodesReaderTest extends SpecificationWithJUnit {
  val thirdPartyPath = "third_party"

  "ImportExternalTargetsFilePartialDependencyNodesReaderTest" should {
    "allBazelDependencyNodes should return node" in {
      val artifact = someCoordinates("some-dep")

      val content = s"""
                      |import_external(
                      |  name = "${artifact.workspaceRuleName}",
                      |  artifact = "${artifact.serialized}",
                      |  jar_sha256 = "",
                      |  srcjar_sha256 = "",
                      |)""".stripMargin

      val reader = new ImportExternalTargetsFilePartialDependencyNodesReader(content, thirdPartyDestination = thirdPartyPath)
      reader.allBazelDependencyNodes() mustEqual Set(PartialDependencyNode(artifact.workspaceRuleName, asCompileDependency(artifact), Set()))
    }

    "allBazelDependencyNodes should support testonly attr after name" in {
      val artifact = someCoordinates("some-dep")

      val content = s"""
                       |import_external(
                       |  name = "${artifact.workspaceRuleName}",
                       |  testonly_ = 1,
                       |  artifact = "${artifact.serialized}",
                       |  jar_sha256 = "",
                       |  srcjar_sha256 = "",
                       |)""".stripMargin

      val reader = ImportExternalTargetsFilePartialDependencyNodesReader(content, thirdPartyDestination = thirdPartyPath)
      reader.allBazelDependencyNodes() mustEqual Set(PartialDependencyNode(artifact.workspaceRuleName, asCompileDependency(artifact), Set()))
    }

  }
}