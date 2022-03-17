package com.wix.build.bazel

import java.io.File

trait BazelLocalWorkspace {

  def overwriteBuildFile(packageName: String, content: String): Unit

  def overwriteThirdPartyImportTargetsFile(thirdPartyGroup: String, content: String): Unit

  def overwriteThirdPartyReposFile(thirdPartyReposContent: String): Unit

  def overwriteLocalArtifactOverridesFile(managedArtifactsContent: String): Unit

  def thirdPartyReposFileContent(): String

  def localArtifactOverridesFileContent(): String

  def buildFileContent(packageName: String): Option[String]

  def thirdPartyImportTargetsFileContent(thirdPartyGroup: String): Option[String]

  def allThirdPartyImportTargetsFilesContent(): Set[String]

  def allThirdPartyImportTargetsFiles(): Map[File, String]

  def allThirdPartyImportTargetsGroups(): Set[String] = {
    allThirdPartyImportTargetsFiles().keys.map { file: File =>
      val fileName = file.getPath.split("/").last
      fileName.substring(0, fileName.lastIndexOf('.'))
    }.toSet
  }

  def allThirdPartyFileLoadedGroups(): Set[String] = {
    val thirdPartyLoadStatementPattern = (
      """^load\("//:""" + thirdPartyPaths.thirdPartyImportFilesPathRoot + """/([_a-zA-Z]+)\.bzl", [_a-zA-Z]+_deps = "dependencies"\)"""
      ).r

    thirdPartyReposFileContent().split("\n").collect {
      case thirdPartyLoadStatementPattern(groupId) => groupId
    }.toSet
  }

  def deleteAllThirdPartyImportTargetsFiles(): Unit

  def thirdPartyOverrides(): ThirdPartyOverrides

  val localWorkspaceName: String

  val thirdPartyPaths: ThirdPartyPaths

}

class ThirdPartyPaths(destination: String) {
  val thirdPartyReposFilePath: String = s"$destination.bzl"
  val thirdPartyImportFilesPathRoot: String = s"$destination"
  val localArtifactOverridesFilePath: String = s"$destination/maven/local_artifact_overrides.bzl"
}
