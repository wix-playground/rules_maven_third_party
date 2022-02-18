package com.wix.build.bazel

import better.files._
import com.wix.build.bazel.FakeLocalBazelWorkspace.{thirdPartyImportFilesPathRoot, thirdPartyReposFilePath}
import com.wix.build.bazel.ThirdPartyOverridesMakers.runtimeOverrides
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import java.io.FileNotFoundException

//noinspection TypeAnnotation
class FileSystemBazelLocalWorkspaceIT extends SpecificationWithJUnit {
  "FileSystemBazelLocalWorkspace" should {
    "throw exception when given filepath does not exist" in {
      val nonExistingPath = file"/not-very-likely-to-exists-path"

      new FileSystemBazelLocalWorkspace(nonExistingPath, new ThirdPartyPaths("third_party")) must throwA[FileNotFoundException]
    }

    "return empty third party repos content if third party repos file does not exists" in new blankWorkspaceCtx {
      new FileSystemBazelLocalWorkspace(blankWorkspaceRootPath, new ThirdPartyPaths("third_party")).thirdPartyReposFileContent() mustEqual ""
    }

    "Get third party repos file content" in new blankWorkspaceCtx {
      val thirdPartyReposContent = "some content"
      blankWorkspaceRootPath.createChild("third_party.bzl").overwrite(thirdPartyReposContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyReposFileContent() mustEqual thirdPartyReposContent
    }

    "Get local artifact overrides file content" in new blankWorkspaceCtx {
      val localArtifactOverridesFileContent = "some content"
      createLocalArtifactOverridesFile(localArtifactOverridesFileContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).localArtifactOverridesFileContent() mustEqual localArtifactOverridesFileContent
    }

    "Get BUILD.bazel file content given package that exist on path" in new blankWorkspaceCtx {
      val packageName = "some/package"
      val buildFile = blankWorkspaceRootPath / packageName / "BUILD.bazel"
      buildFile.createIfNotExists(createParents = true)
      val buildFileContent = "some build content"
      buildFile.overwrite(buildFileContent)

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
      (blankWorkspaceRootPath / "bazel_migration" / "third_party_targets.overrides")
        .createIfNotExists(createParents = true)
        .overwrite(json)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyOverrides() mustEqual originalOverrides
    }

    "write third party repos file content" in new blankWorkspaceCtx {
      val thirdPartyReposFile = blankWorkspaceRootPath.createChild(thirdPartyReposFilePath)
      val newContent = "newContent"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyReposFile(newContent)

      thirdPartyReposFile.contentAsString mustEqual newContent
    }

    "write local artifact overrides file" in new blankWorkspaceCtx {
      val localArtifactOverridesFile = createLocalArtifactOverridesFile()
      val newContent = "newContent"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteLocalArtifactOverridesFile(newContent)
      localArtifactOverridesFile.contentAsString mustEqual newContent
    }

    "not fail if local artifact overrides file does not exist" in new blankWorkspaceCtx {
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteLocalArtifactOverridesFile("")
    }

    "write BUILD.bazel file content, even if the package did not exist" in new blankWorkspaceCtx {
      val newPackage = "some/new/package"
      val buildFileContent = "some build file content"

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteBuildFile(newPackage, buildFileContent)
      val buildFile = blankWorkspaceRootPath / newPackage / "BUILD.bazel"

      buildFile.exists aka "build file exists" must beTrue
      buildFile.contentAsString mustEqual buildFileContent
    }

    "allow reading a Third Party Import Targets File after creating it" in new blankWorkspaceCtx {
      val newContent = "newContent"
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).overwriteThirdPartyImportTargetsFile(someGroup, newContent)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).thirdPartyImportTargetsFileContent(someGroup) must beSome(newContent)
    }

    "allow overwriting and then reading the contents of Third Party Import Targets File" in new blankWorkspaceCtx {
      val thirdPartyImportFile = blankWorkspaceRootPath.createChild(thirdPartyImportFilesPathRoot, true).createChild(s"$someGroup.bzl")
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
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).allThirdPartyImportTargetsFilesContent() must be empty
    }

    "Get All Third Party Import Targets Files content" in new blankWorkspaceCtx {
      writeImportFiles(Map(someGroup -> thirdPartyImportFileContent,
        anotherGroup -> anotherThirdPartyImportFileContent))

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).allThirdPartyImportTargetsFilesContent() must containTheSameElementsAs(Seq(thirdPartyImportFileContent, anotherThirdPartyImportFileContent))
    }

    "Get All Third Party Import Targets Files content except manually managed files" in new blankWorkspaceCtx {
      writeImportFiles(Map(someGroup -> thirdPartyImportFileContent))
      writeCustomImportFiles(Map(anotherGroup -> anotherThirdPartyImportFileContent))

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).allThirdPartyImportTargetsFilesContent() must containTheSameElementsAs(Seq(thirdPartyImportFileContent))
    }

    "return empty workspace name if workspace does not exist" in new blankWorkspaceCtx {
      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).localWorkspaceName mustEqual ""
    }

    "return workspace name" in new blankWorkspaceCtx {
      val workspaceName = "some_workspace_name"
      (blankWorkspaceRootPath / "WORKSPACE")
        .createIfNotExists(createParents = true)
        .overwrite(
          s"""
             |workspace(name = "$workspaceName")
             |load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
                    """.stripMargin)

      aFileSystemBazelLocalWorkspace(blankWorkspaceRootPath).localWorkspaceName mustEqual workspaceName
    }


  }

  trait blankWorkspaceCtx extends Scope {
    val blankWorkspaceRootPath = File.newTemporaryDirectory("bazel")
    blankWorkspaceRootPath
      .toJava.deleteOnExit()

    val thirdPartyImportFileContent = "some content"
    val anotherThirdPartyImportFileContent = "some other content"
    val someGroup = "some_group"
    val anotherGroup = "another_group"

    def createLocalArtifactOverridesFile(content: String = ""): File = {
      blankWorkspaceRootPath
        .createChild("third_party", asDirectory = true)
        .createChild("maven", asDirectory = true)
        .createChild("local_artifact_overrides.bzl")
        .overwrite(content)
    }

    def writeImportFiles(files: Map[String, String]) = {
      val thirdPartyImportFilesDir = blankWorkspaceRootPath.createChild(thirdPartyImportFilesPathRoot, true)

      files.foreach { f =>
        val (group_name, content) = f
        val thirdPartyImportFile = thirdPartyImportFilesDir.createChild(s"$group_name.bzl")
        thirdPartyImportFile.overwrite(content)
      }
    }

    def writeCustomImportFiles(files: Map[String, String]) = {
      val thirdPartyCustomImportFilesDir = blankWorkspaceRootPath.createChild(thirdPartyImportFilesPathRoot + "/custom", true)

      files.foreach { f =>
        val (group_name, content) = f
        val thirdPartyImportFile = thirdPartyCustomImportFilesDir.createChild(s"$group_name.bzl")
        thirdPartyImportFile.overwrite(content)
      }

    }
  }

  private def aFileSystemBazelLocalWorkspace(on: File) = {
    new FileSystemBazelLocalWorkspace(on, new ThirdPartyPaths("third_party"))
  }
}
