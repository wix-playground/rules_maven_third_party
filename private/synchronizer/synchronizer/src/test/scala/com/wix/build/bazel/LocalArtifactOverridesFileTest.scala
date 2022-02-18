package com.wix.build.bazel

import com.wix.build.maven._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class LocalArtifactOverridesFileTest extends SpecWithJUnit {

  "updateContent" should {

    "add new artifact to empty LOCAL_OVERRIDE_DEPS list" in new Context {
      val dependency = Dependency(Coordinates(groupId, artifactId, version), MavenScope.Test)

      LocalArtifactOverridesFile.updateContent(emptyContent, Set(dependency)) mustEqual
        aContentHavingArtifact(groupId, artifactId, version)
    }

    "add new artifact to non empty LOCAL_OVERRIDE_DEPS list" in new Context {
      val anotherGroupId = "another-group-id"
      val anotherArtifactId = "another-artifact-id"
      val anotherVersion = "another-version"
      val exclusionGroupId = "exclusion-group-id"
      val exclusionArtifactId = "exclusion-artifact-id"
      val alias = "alias"
      val tag = "tag"

      val content = aContentHavingArtifact(groupId, artifactId, version)

      val anotherDependency = Dependency(
        Coordinates(anotherGroupId, anotherArtifactId, anotherVersion),
        MavenScope.Test,
        exclusions = Set(Exclusion(exclusionGroupId, exclusionArtifactId)),
        tags = Set(tag),
        aliases = Set(alias)
      )

      LocalArtifactOverridesFile.updateContent(content, Set(anotherDependency)) must beEqualTo(
        s"""$loadStatement
           |
           |LOCAL_OVERRIDE_DEPS = [
           |    maven.artifact(group = "$groupId", artifact = "$artifactId", version = "$version"),
           |    ${aMavenArtifactWith(anotherGroupId, anotherArtifactId, anotherVersion, Exclusion(exclusionGroupId, exclusionArtifactId), tag, alias)},
           |]
           |""".stripMargin
      )
    }

    "not add the same artifact twice" in new Context {
      val content = aContentHavingArtifact(groupId, artifactId, version)

      val dependency = Dependency(Coordinates(groupId, artifactId, version), MavenScope.Test)

      LocalArtifactOverridesFile.updateContent(content, Set(dependency)) must beEqualTo(content)
    }
  }

  trait Context extends Scope {
    val groupId = "group-id"
    val artifactId = "artifact-id"
    val version = "version"
    val loadStatement = "load-statement"
    val emptyContent =
      s"""$loadStatement
         |
         |LOCAL_OVERRIDE_DEPS = []
         |""".stripMargin

    def aContentHavingArtifact(groupId: String, artifactId: String, version: String): String = {
      s"""$loadStatement
         |
         |LOCAL_OVERRIDE_DEPS = [
         |    maven.artifact(group = "$groupId", artifact = "$artifactId", version = "$version"),
         |]
         |""".stripMargin
    }

    def aMavenArtifactWith(groupId: String,
                           artifactId: String,
                           version: String,
                           exclusion: Exclusion,
                           tag: String,
                           alias: String): String = {

      "maven.artifact(" +
        s"""group = "$groupId", """ +
        s"""artifact = "$artifactId", """ +
        s"""version = "$version", """ +
        s"""exclusions = [maven.exclusion(artifact = "${exclusion.artifactId}", group = "${exclusion.groupId}")], """ +
        s"""tags = [$tag], """ +
        s"""aliases = [$alias])""".stripMargin
    }
  }

}
