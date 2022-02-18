package com.wix.build.sync.cli

import com.wix.build.maven._
import org.specs2.mutable.SpecWithJUnit

class RulesJvmExternalDomainTest extends SpecWithJUnit {
  "RulesJvmExternal" should {
    "convert dependency json string to Maven dependency" in {
      val groupId = "org.apache.kafka"
      val artifactId = "kafka_2.12"
      val version = "2.4.1"
      val exclusionGroupId = "log4j"
      val exclusionArtifactId = "log4j"
      val input =
        s"""{
           |  "group": "$groupId",
           |  "artifact": "$artifactId",
           |  "version": "$version",
           |  "exclusions": [
           |    {
           |      "group": "$exclusionGroupId",
           |      "artifact": "$exclusionArtifactId"
           |    }
           |  ]
           |}""".stripMargin

      val mavenDep: Dependency = RulesJvmExternalDomain.convertJsonStringToMavenDep(input)
      mavenDep mustEqual Dependency(
        coordinates = Coordinates(groupId, artifactId, version),
        scope = MavenScope.Compile,
        exclusions = Set(Exclusion(exclusionGroupId, exclusionArtifactId))
      )
    }
  }
}
