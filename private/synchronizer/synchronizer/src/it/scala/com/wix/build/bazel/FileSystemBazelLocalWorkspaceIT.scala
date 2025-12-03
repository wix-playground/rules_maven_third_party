package com.wix.build.bazel

import com.wix.build.bazel.FakeLocalBazelWorkspace.{thirdPartyImportFilesPathRoot, thirdPartyReposFilePath}
import com.wix.build.bazel.ThirdPartyOverridesMakers.runtimeOverrides
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import java.io.{File, FileNotFoundException}
import java.nio.file.{Files, Paths}

//noinspection TypeAnnotation
class FileSystemBazelLocalWorkspaceIT extends SpecificationWithJUnit {
  private val destination = "third_party"
  val thirdPartyPaths = new ThirdPartyPaths(destination, s"$destination.bzl", DestinationPackage.resolveFromDestination(destination))

  "FileSystemBazelLocalWorkspace" should {
    "throw exception when given filepath does not exist" in {
      val nonExistingPath = Paths.get("/not-very-likely-to-exists-path")

      new FileSystemBazelLocalWorkspace(nonExistingPath.toFile, thirdPartyPaths) must throwA[FileNotFoundException]
    }

    "return initial skeleton for third party repos content if third party repos file does not exists" in new blankWorkspaceCtx {
      new FileSystemBazelLocalWorkspace(blankWorkspaceRootPath, thirdPartyPaths).thirdPartyReposFileContent() mustEqual "def dependencies():"
    }

    "Get third party repos file content" in new blankWorkspaceCtx {
      val thirdPartyReposContent = "some content"

      Files.writeString(blankWorkspaceRootPath.toPath.resolve("third_party.bzl"), thirdPartyReposContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyReposFileContent() mustEqual thirdPartyReposContent
    }

    "Get local artifact overrides file content" in new blankWorkspaceCtx {
      val localArtifactOverridesFileContent = "some content"
      createLocalArtifactOverridesFile(localArtifactOverridesFileContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).localArtifactOverridesFileContent() mustEqual localArtifactOverridesFileContent
    }

    "Get BUILD.bazel file content given package that exist on path" in new blankWorkspaceCtx {
      val packageName = "some/package"
      val buildFile = Paths.get(blankWorkspaceRootPath.toString, packageName, "BUILD.bazel")
      buildFile.getParent.toFile.mkdirs()
      val buildFileContent = "some build content"
      Files.writeString(buildFile, buildFileContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).buildFileContent(packageName) must beSome(buildFileContent)
    }

    "return None if BUILD.bazel file does not exists" in new blankWorkspaceCtx {
      val packageName = "some/non-existing/package"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).buildFileContent(packageName) must beNone
    }

    "return empty third party overrides if no such file exists" in new blankWorkspaceCtx {
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyOverrides() mustEqual ThirdPartyOverrides.empty
    }

    "return serialized third party overrides according to json in local workspace" in new blankWorkspaceCtx {
      val originalOverrides = runtimeOverrides(OverrideCoordinates("some.group", "some-artifact"), "label")
      val json = {
        val objectMapper = ThirdPartyOverridesReader.mapper
        objectMapper.writeValueAsString(originalOverrides)
      }
      val overridesFile = Paths.get(blankWorkspaceRootPath.getPath, "bazel_migration", "third_party_targets.overrides").toFile
      overridesFile.getParentFile.mkdirs()
      Files.writeString(overridesFile.toPath, json)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyOverrides() mustEqual originalOverrides
    }

    "write third party repos file content" in new blankWorkspaceCtx {
      val thirdPartyReposFile = blankWorkspaceRootPath.toPath.resolve(thirdPartyReposFilePath)
      val newContent = "newContent"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyReposFile(newContent)

      Files.readString(thirdPartyReposFile) mustEqual newContent
    }

    "write local artifact overrides file" in new blankWorkspaceCtx {
      val localArtifactOverridesFile = createLocalArtifactOverridesFile()
      val newContent = "newContent"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteLocalArtifactOverridesFile(newContent)

      Files.readString(localArtifactOverridesFile.toPath) mustEqual newContent
    }

    "not fail if local artifact overrides file does not exist" in new blankWorkspaceCtx {
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteLocalArtifactOverridesFile("")
    }

    "write BUILD.bazel file content, even if the package did not exist" in new blankWorkspaceCtx {
      val newPackage = "some/new/package"
      val buildFileContent = "some build file content"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteBuildFile(newPackage, buildFileContent)
      val buildFile = Paths.get(blankWorkspaceRootPath.getPath, newPackage, "BUILD.bazel")

      buildFile.toFile.exists aka "build file exists" must beTrue
      Files.readString(buildFile) mustEqual buildFileContent
    }

    "allow reading a Third Party Import Targets File after creating it" in new blankWorkspaceCtx {
      val newContent = "newContent"
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyImportTargetsFile(someGroup, newContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyImportTargetsFileContent(someGroup) must beSome(newContent)
    }

    "allow overwriting and then reading the contents of Third Party Import Targets File" in new blankWorkspaceCtx {
      val thirdPartyImportFile = blankWorkspaceRootPath.toPath.resolve(thirdPartyImportFilesPathRoot)
      thirdPartyImportFile.toFile.mkdirs()
      val newContent = "newContent"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyImportTargetsFile(someGroup, newContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyImportTargetsFileContent(someGroup) must beSome(newContent)
    }

    "delete a Third Party Import Targets File if writing empty content to it" in new blankWorkspaceCtx {
      val emptyContent = ""
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyImportTargetsFile(someGroup, "whatever")
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyImportTargetsFile(someGroup, emptyContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyImportTargetsFileContent(someGroup) must beNone
    }

    "not fail if deleting a non existent Third Party Import Targets File" in new blankWorkspaceCtx {
      val emptyContent = ""
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyImportTargetsFile(someGroup, emptyContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyImportTargetsFileContent(someGroup) must beNone
    }

    "Get Empty Third Party Import Targets Files content" in new blankWorkspaceCtx {
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).allThirdPartyImportTargetsFilesContent() must beEmpty
    }

    "Get All Third Party Import Targets Files content" in new blankWorkspaceCtx {
      writeImportFiles(Map(someGroup -> thirdPartyImportFileContent,
        anotherGroup -> anotherThirdPartyImportFileContent))

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath)
        .allThirdPartyImportTargetsFilesContent() must containTheSameElementsAs(Seq(thirdPartyImportFileContent, anotherThirdPartyImportFileContent))
    }

    "Get All Third Party Import Targets Files content except manually managed files" in new blankWorkspaceCtx {
      writeImportFiles(Map(someGroup -> thirdPartyImportFileContent))
      writeCustomImportFiles(Map(anotherGroup -> anotherThirdPartyImportFileContent))

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath)
        .allThirdPartyImportTargetsFilesContent() must containTheSameElementsAs(Seq(thirdPartyImportFileContent))
    }

    "return empty workspace name if workspace does not exist" in new blankWorkspaceCtx {
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).localWorkspaceName mustEqual ""
    }

    "return workspace name" in new blankWorkspaceCtx {
      val workspaceName = "some_workspace_name"
      val workspacePath = Paths.get(blankWorkspaceRootPath.getPath, "WORKSPACE")

      Files.writeString(
        workspacePath,
        s"""
          |workspace(name = "$workspaceName")
          |load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
                    """.stripMargin
      )

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).localWorkspaceName mustEqual workspaceName
    }


  }

  trait blankWorkspaceCtx extends Scope {
    val blankWorkspaceRootPath = Files.createTempDirectory("bazel").toFile
    blankWorkspaceRootPath.deleteOnExit()

    val thirdPartyImportFileContent = "some content"
    val anotherThirdPartyImportFileContent = "some other content"
    val someGroup = "some_group"
    val anotherGroup = "another_group"

    def createLocalArtifactOverridesFile(content: String = ""): File = {
      val overridesFile = Paths.get(blankWorkspaceRootPath.getPath, "third_party/maven/local_artifact_overrides.bzl")
      overridesFile.toFile.getParentFile.mkdirs()
      Files.writeString(overridesFile, content).toFile
    }

    def writeImportFiles(files: Map[String, String]) = {
      val thirdPartyImportFilesDir = blankWorkspaceRootPath.toPath.resolve(thirdPartyImportFilesPathRoot)
      thirdPartyImportFilesDir.toFile.mkdirs()

      files.foreach { case (group_name, content) =>
        Files.writeString(thirdPartyImportFilesDir.resolve(s"$group_name.bzl"), content)
      }
    }

    def writeCustomImportFiles(files: Map[String, String]) = {
      val thirdPartyCustomImportFilesDir = blankWorkspaceRootPath.toPath.resolve(thirdPartyImportFilesPathRoot + "/custom")
      thirdPartyCustomImportFilesDir.toFile.mkdirs()

      files.foreach { case (group_name, content) =>
        Files.writeString(thirdPartyCustomImportFilesDir.resolve(s"$group_name.bzl"), content)
      }

    }
  }

  private def aFileSystemBazelLocalWorkspace(on: File) = {
    val destination = "third_party"
    new FileSystemBazelLocalWorkspace(
      on,
      new ThirdPartyPaths(destination, s"$destination.bzl", DestinationPackage.resolveFromDestination(destination))
    )
  }
}
