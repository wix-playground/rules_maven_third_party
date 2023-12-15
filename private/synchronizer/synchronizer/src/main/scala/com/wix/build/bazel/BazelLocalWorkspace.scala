package com.wix.build.bazel

import java.io.File

trait BazelLocalWorkspace {

  def overwriteBuildFile(packageName: String, content: String): Unit

  def overwriteThirdPartyImportTargetsFile(thirdPartyGroup: String, content: String): Unit

  def overwriteThirdPartyReposFile(thirdPartyReposContent: String): Unit

  def writeReceipt(content: String): Unit

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
      """^load\("""" + thirdPartyPaths.destinationPackage.bazelPackage + """/([_a-zA-Z]+)\.bzl", [_a-zA-Z]+_deps = "dependencies"\)"""
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

class ThirdPartyPaths(destination: String, val destinationPackage: DestinationPackage) {
  // TODO: remove after migration is finished
  def this(destination: String) = this(destination, DestinationPackage.resolveFromDestination(destination))

  val thirdPartyReposFilePath: String = s"$destination.bzl"
  val receiptPath: String = s"$destination-receipt.txt"
  val thirdPartyImportFilesPathRoot: String = s"$destination"
  val localArtifactOverridesFilePath: String = s"$destination/maven/local_artifact_overrides.bzl"
}

case class DestinationPackage(bazelPackage: String)

object DestinationPackage {
  def resolveFromDestination(destination: String): DestinationPackage = {
    val packagePath = s"/${destination}"
    val lastIndexOfSlash = packagePath.lastIndexOf("/")
    val parts = packagePath.splitAt(lastIndexOfSlash)
    val thirdPartyPackage = s"//${parts._1.stripPrefix("/")}:${parts._2.stripPrefix("/")}"
    DestinationPackage(thirdPartyPackage)
  }
}