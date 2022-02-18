package com.wix.build.bazel

import com.wix.build.bazel.CoordinatesTestBuilders._
import com.wix.build.bazel.ThirdPartyReposFile.{Builder, serializedImportExternalTargetsFileMethodCall, serializedLoadImportExternalTargetsFile}
import com.wix.build.maven.{Coordinates, MavenMakers, Packaging}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class ThirdPartyReposFileTest extends SpecificationWithJUnit {

  val jars = List(artifactA, artifactB, artifactC)
  val thirdPartyPath = "third_party"

  "third party repos file parser" should {
    trait ctx extends Scope {
      val mavenJarCoordinates = Coordinates.deserialize("some.group:some-artifact:some-version")
      val thirdPartyReposWithJarWorkspaceRule =
        s"""
           |if native.existing_rule("maven_jar_name") == None:
           |    jar_workspace_rule(
           |        name = "maven_jar_name",
           |        artifact = "${mavenJarCoordinates.serialized}"
           |    )""".stripMargin

      val protoCoordinates = Coordinates.deserialize("some.group:some-artifact:zip:proto:some-version")
      val thirdPartyReposWithArchiveWorkspaceRule =
        s"""
           |if native.existing_rule("proto_name") == None:
           |    zip_workspace_rule(
           |        name = "proto_name",
           |        artifact = "${protoCoordinates.serialized}"
           |    )""".stripMargin

      val combinedThirdPartyRepos = thirdPartyReposWithArchiveWorkspaceRule + thirdPartyReposWithJarWorkspaceRule
    }

    "extract coordinates from jar rule" in new ctx {
      val coordinates = ThirdPartyReposFile.Parser(thirdPartyReposWithJarWorkspaceRule).allMavenCoordinates

      coordinates must contain(mavenJarCoordinates)
    }

    "extract coordinates from archive rule" in new ctx {
      val coordinates = ThirdPartyReposFile.Parser(thirdPartyReposWithArchiveWorkspaceRule).allMavenCoordinates

      coordinates must contain(protoCoordinates)
    }

    "extract multiple coordinates from various rules" in new ctx {
      val rules = ThirdPartyReposFile.Parser(combinedThirdPartyRepos).allMavenCoordinates
      rules must containTheSameElementsAs(Seq(protoCoordinates, mavenJarCoordinates))
    }
  }

  "third party repos file builder" should {
    "update maven archive version in case one with same artifactId and groupId defined in third party repos" in {
      val archives = jars.map(_.copy(packaging = Packaging("zip")))
      val thirdPartyRepos = createThirdPartyReposWith(archives)
      val newHead = archives.head.copy(version = "5.0")
      val newArchives = newHead +: archives.tail
      val expectedThirdPartyRepos = createThirdPartyReposWith(newArchives)
      ThirdPartyReposFile.Builder(thirdPartyRepos).withMavenArtifact(newHead).content mustEqual expectedThirdPartyRepos
    }

    "insert new maven archive in case there is no maven jar with same artifact id and group id" in {
      val archives = jars.map(_.copy(packaging = Packaging("zip")))
      val thirdPartyRepos = createThirdPartyReposWith(archives)
      val dontCareVersion = "3.0"
      val newJar = Coordinates("new.group", "new-artifact", dontCareVersion)
      val expectedThirdPartyRepos =
        s"""$thirdPartyRepos
           |
           |${WorkspaceRule.of(newJar).serialized}
           |""".stripMargin

      ThirdPartyReposFile.Builder(thirdPartyRepos).withMavenArtifact(newJar).content mustEqual expectedThirdPartyRepos
    }

    "do nothing in case one with same groupId but different artifactId is defined in third party repos" in {
      val thirdPartyRepos = createThirdPartyReposWithLoadStatementFor(jars)
      val newHead = jars.head.copy(artifactId = "bla-artifact")
      ThirdPartyReposFile.Builder(thirdPartyRepos).withLoadStatementsFor(newHead, thirdPartyPath).content mustEqual thirdPartyRepos
    }

    "insert new load statement in case there is no load statement with same group id" in {
      val thirdPartyRepos = createThirdPartyReposWithLoadStatementFor(jars)
      val dontCareArtifactId = "some-artifact"
      val dontCareVersion = "3.0"
      val newJar = Coordinates("new.group", dontCareArtifactId, dontCareVersion)

      val expectedThirdPartyRepos =
        s"""load("//:third_party/new_group.bzl", new_group_deps = "dependencies")
           |
           |$thirdPartyRepos
           |
           |    new_group_deps()
           |""".stripMargin

      ThirdPartyReposFile.Builder(thirdPartyRepos).withLoadStatementsFor(newJar, thirdPartyPath).content mustEqual expectedThirdPartyRepos
    }

    "ignore war dependencies" in {
      val warDependnecy = MavenMakers.someCoordinates("some-dep", Packaging("war"))
      ThirdPartyReposFile.Builder("foo").fromCoordinates(warDependnecy, thirdPartyPath) mustEqual Builder("foo")
    }

    "delete load statement" in {
      val thirdPartyRepos = createThirdPartyReposWithLoadStatementFor(List(artifactA, artifactB, artifactC))
      val expectedThirdPartyRepos = createThirdPartyReposWithLoadStatementFor(List(artifactA, artifactB))

      ThirdPartyReposFile.Builder(thirdPartyRepos).removeGroupIds(artifactC.groupId, thirdPartyPath).content mustEqual expectedThirdPartyRepos
    }

    "delete 2 consecutive load statements which have no empty line in between them" in {
      val thirdPartyRepos = createThirdPartyReposWithLoadStatementFor(List(artifactA, artifactB, artifactC, artifactD), insertOnlyOneNewLine = true)
      val expectedThirdPartyRepos = createThirdPartyReposWithLoadStatementFor(List(artifactA, artifactD))

      ThirdPartyReposFile.Builder(thirdPartyRepos)
        .removeGroupIds(artifactB.groupId, thirdPartyPath)
        .removeGroupIds(artifactC.groupId, thirdPartyPath).content mustEqual expectedThirdPartyRepos
    }
  }

  private def createThirdPartyReposWith(coordinates: List[Coordinates]) = {
    val firstJar: Coordinates = coordinates.head
    val restOfJars = coordinates.tail

    val rest = restOfJars.map(WorkspaceRule.of).map(_.serialized).mkString("\n\n")

    s"""load("@core_server_build_tools//:macros.bzl", "maven_archive", "maven_proto")
       |
       |def third_party_dependencies():
       |
       |${WorkspaceRule.of(firstJar).serialized}
       |
       |$rest
       |
       |### THE END""".stripMargin
  }


  private def createThirdPartyReposWithLoadStatementFor(coordinates: List[Coordinates], insertOnlyOneNewLine: Boolean = false) = {
    def insertOnlyOneNewLineIfRequired() = insertOnlyOneNewLine match {
      case true => "\n"
      case false => "\n\n"
    }

    val firstJar: Coordinates = coordinates.head
    val restOfJars = coordinates.tail

    val restLoads = restOfJars.map(serializedLoadImportExternalTargetsFile(_, thirdPartyPath)).mkString(insertOnlyOneNewLineIfRequired)
    val restCalls = restOfJars.map(serializedImportExternalTargetsFileMethodCall).mkString(insertOnlyOneNewLineIfRequired)


    s"""load("@core_server_build_tools//:macros.bzl", "maven_archive", "maven_proto")
       |
       |${serializedLoadImportExternalTargetsFile(firstJar, thirdPartyPath)}
       |
       |$restLoads
       |
       |def third_party_dependencies():
       |
       |${serializedImportExternalTargetsFileMethodCall(firstJar)}
       |
       |$restCalls
       |
       |### THE END""".stripMargin
  }

}

object CoordinatesTestBuilders {
  val artifactsVersion = "1.102.0-SNAPSHOT"

  val artifactA: Coordinates = Coordinates(
    groupId = "com.wix.framework",
    artifactId = "example-artifact",
    version = "1.101.0-SNAPSHOT"
  )

  val artifactB = Coordinates(
    groupId = "com.google.guava",
    artifactId = "guava",
    version = "19.0"
  )

  val artifactC = Coordinates(
    groupId = "groupIdC",
    artifactId = "artifactId",
    version = "version")

  val artifactD = Coordinates(
    groupId = "groupIdD",
    artifactId = "artifactId",
    version = "version")
}
