package com.wix.build.bazel

import better.files.File

import java.io.FileNotFoundException

class FileSystemBazelLocalWorkspace(root: File, val thirdPartyPaths: ThirdPartyPaths) extends BazelLocalWorkspace {
  val localWorkspaceName: String = {
    val workspaceFileContent = contentIfExistsOf(root / "WORKSPACE")
    val validWorkspaceWith = """(?s).*workspace\s*\(\s*name\s*=\s*"([^"]+)"\s*\).*""".r

    workspaceFileContent match {
      case Some(validWorkspaceWith(name)) => name
      case _ => ""
    }
  }

  private val ThirdPartyOverridesPath = "bazel_migration/third_party_targets.overrides"

  validate()

  override def overwriteBuildFile(packageName: String, content: String): Unit = {
    val buildFilePath = root / packageName / "BUILD.bazel"
    buildFilePath.createIfNotExists(createParents = true)
    buildFilePath.overwrite(content)
  }

  override def overwriteThirdPartyReposFile(thirdPartyReposContent: String): Unit =
    (root / thirdPartyPaths.thirdPartyReposFilePath).overwrite(thirdPartyReposContent)

  override def overwriteLocalArtifactOverridesFile(managedArtifactsContent: String): Unit = {
    val path = root / thirdPartyPaths.localArtifactOverridesFilePath
    if (path.exists) {
      path.overwrite(managedArtifactsContent)
    }
  }

  override def overwriteThirdPartyImportTargetsFile(thirdPartyGroup: String, content: String): Unit = {
    val targetsFile = root / s"${thirdPartyPaths.thirdPartyImportFilesPathRoot}/$thirdPartyGroup.bzl"
    content match {
      case "" => if (targetsFile.exists) targetsFile.delete()
      case _ =>
        targetsFile.createIfNotExists(createParents = true)
        targetsFile.overwrite(content)
    }
  }

  override def thirdPartyReposFileContent(): String = contentIfExistsOf(root / thirdPartyPaths.thirdPartyReposFilePath).getOrElse("")

  override def localArtifactOverridesFileContent(): String = contentIfExistsOf(root / thirdPartyPaths.localArtifactOverridesFilePath).getOrElse("")

  override def buildFileContent(packageName: String): Option[String] = contentIfExistsOf(root / packageName / "BUILD.bazel")

  override def thirdPartyImportTargetsFileContent(thirdPartyGroup: String): Option[String] = contentIfExistsOf(root / thirdPartyPaths.thirdPartyImportFilesPathRoot / s"$thirdPartyGroup.bzl")

  override def allThirdPartyImportTargetsFilesContent(): Set[String] = {
    allThirdPartyImportTargetsFiles().values.toSet
  }

  override def allThirdPartyImportTargetsFiles(): Map[File, String] = {
    val thirdPartyLocation = root / thirdPartyPaths.thirdPartyImportFilesPathRoot
    thirdPartyLocation.createIfNotExists(asDirectory = true, createParents = true)
    val files = thirdPartyLocation.glob(s"$thirdPartyLocation/*.bzl", includePath = false)
    files.map(f => f -> contentIfExistsOf(f).get).toMap
  }

  override def thirdPartyOverrides(): ThirdPartyOverrides = {
    contentIfExistsOf(root / ThirdPartyOverridesPath)
      .map(ThirdPartyOverridesReader.from)
      .getOrElse(ThirdPartyOverrides.empty)
  }

  private def contentIfExistsOf(filePath: File) =
    if (filePath.exists) Some(filePath.contentAsString) else None

  private def validate(): Unit = {
    if (!root.exists)
      throw new FileNotFoundException(root.pathAsString)
  }

  override def deleteAllThirdPartyImportTargetsFiles(): Unit = {
    allThirdPartyImportTargetsFiles().keys.foreach(_.delete())
  }
}

