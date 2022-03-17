package com.wix.build.bazel

import java.io.{File, FileNotFoundException, FilenameFilter}
import java.nio.file.{Files, Paths}

class FileSystemBazelLocalWorkspace(root: File, val thirdPartyPaths: ThirdPartyPaths) extends BazelLocalWorkspace {
  val localWorkspaceName: String = {
    val workspaceFileContent = contentIfExistsOf(Paths.get(root.getPath, "WORKSPACE").toFile)
    val validWorkspaceWith = """(?s).*workspace\s*\(\s*name\s*=\s*"([^"]+)"\s*\).*""".r

    workspaceFileContent match {
      case Some(validWorkspaceWith(name)) => name
      case _ => ""
    }
  }

  private val ThirdPartyOverridesPath = "bazel_migration/third_party_targets.overrides"

  validate()

  override def overwriteBuildFile(packageName: String, content: String): Unit = {
    val buildFilePath = Paths.get(root.getPath, packageName, "BUILD.bazel")
    buildFilePath.getParent.toFile.mkdirs()
    Files.writeString(buildFilePath, content)
  }

  override def overwriteThirdPartyReposFile(thirdPartyReposContent: String): Unit = {
    val path = Paths.get(root.getPath, thirdPartyPaths.thirdPartyReposFilePath)
    Files.writeString(path, thirdPartyReposContent)
  }

  override def overwriteLocalArtifactOverridesFile(managedArtifactsContent: String): Unit = {
    val path = Paths.get(root.getPath, thirdPartyPaths.localArtifactOverridesFilePath)
    if (path.toFile.exists) {
      Files.writeString(path, managedArtifactsContent)
    }
  }

  override def overwriteThirdPartyImportTargetsFile(thirdPartyGroup: String, content: String): Unit = {
    val targetsFile = Paths.get(root.getPath, s"${thirdPartyPaths.thirdPartyImportFilesPathRoot}/$thirdPartyGroup.bzl")

    if (content.isEmpty) {
      if (targetsFile.toFile.exists) targetsFile.toFile.delete()
    }
    else {
      targetsFile.toFile.getParentFile.mkdir()
      Files.writeString(targetsFile, content)
    }
  }

  override def thirdPartyReposFileContent(): String =
    contentIfExistsOf(Paths.get(root.getPath, thirdPartyPaths.thirdPartyReposFilePath).toFile).getOrElse("")

  override def localArtifactOverridesFileContent(): String =
    contentIfExistsOf(Paths.get(root.getPath, thirdPartyPaths.localArtifactOverridesFilePath).toFile).getOrElse("")

  override def buildFileContent(packageName: String): Option[String] =
    contentIfExistsOf(Paths.get(root.getPath, packageName, "BUILD.bazel").toFile)

  override def thirdPartyImportTargetsFileContent(thirdPartyGroup: String): Option[String] = contentIfExistsOf(
    Paths.get(root.getPath, thirdPartyPaths.thirdPartyImportFilesPathRoot, s"$thirdPartyGroup.bzl").toFile
  )

  override def allThirdPartyImportTargetsFilesContent(): Set[String] = {
    allThirdPartyImportTargetsFiles().values.toSet
  }

  override def allThirdPartyImportTargetsFiles(): Map[File, String] = {
    val thirdPartyLocation = Paths.get(root.getPath, thirdPartyPaths.thirdPartyImportFilesPathRoot)
    if (!thirdPartyLocation.toFile.exists())
      thirdPartyLocation.toFile.mkdirs()

    thirdPartyLocation.toFile.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = dir == thirdPartyLocation.toFile && name.endsWith(".bzl")
    }).map(f => f -> contentIfExistsOf(f).get).toMap
  }

  override def thirdPartyOverrides(): ThirdPartyOverrides = {
    contentIfExistsOf(Paths.get(root.getPath, ThirdPartyOverridesPath).toFile)
      .map(ThirdPartyOverridesReader.from)
      .getOrElse(ThirdPartyOverrides.empty)
  }

  private def contentIfExistsOf(filePath: File) =
    if (filePath.exists) Some(Files.readString(filePath.toPath)) else None

  private def validate(): Unit = {
    if (!root.exists)
      throw new FileNotFoundException(root.getPath)
  }

  override def deleteAllThirdPartyImportTargetsFiles(): Unit = {
    allThirdPartyImportTargetsFiles().keys.foreach(_.delete())
  }
}

