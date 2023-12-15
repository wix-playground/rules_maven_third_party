package com.wix.build.bazel

import java.io.File
import scala.collection.mutable

class FakeLocalBazelWorkspace(sourceFiles: mutable.Map[String, String] = mutable.Map.empty,
                              val localWorkspaceName: String = "",
                              val thirdPartyPaths: ThirdPartyPaths = new ThirdPartyPaths("third_party", DestinationPackage.resolveFromDestination("third_party")))
  extends BazelLocalWorkspace {

  import thirdPartyPaths._

  // since FakeLocalBazelWorkspace is already stateful - I allowed another state.
  // on next revision of SynchronizerAcceptanceTest - we will introduce stateless FakeWorkspace
  private var overrides = ThirdPartyOverrides.empty

  def setThirdPartyOverrides(overrides: ThirdPartyOverrides): Unit = {
    this.overrides = overrides
  }

  override def thirdPartyReposFileContent(): String =
    sourceFiles.getOrElse(thirdPartyReposFilePath, "")

  override def localArtifactOverridesFileContent(): String =
    sourceFiles.getOrElse(localArtifactOverridesFilePath, "")

  override def overwriteThirdPartyReposFile(skylarkFileContent: String): Unit =
    sourceFiles.put(thirdPartyReposFilePath, skylarkFileContent)

  def writeReceipt(content: String): Unit = ???

  override def overwriteLocalArtifactOverridesFile(managedArtifactsContent: String): Unit = {
    sourceFiles.put(localArtifactOverridesFilePath, managedArtifactsContent)
  }

  override def overwriteThirdPartyImportTargetsFile(thirdPartyGroup: String, thirdPartyReposContent: String): Unit = {
    val fileKey = s"$thirdPartyImportFilesPathRoot/$thirdPartyGroup.bzl"
    thirdPartyReposContent match {
      case "" => sourceFiles.remove(fileKey)
      case _ => sourceFiles.put(fileKey, thirdPartyReposContent)
    }
  }

  override def buildFileContent(packageName: String): Option[String] =
    sourceFiles.get(packageName + "/BUILD.bazel")

  override def thirdPartyImportTargetsFileContent(thirdPartyGroup: String): Option[String] =
    sourceFiles.get(s"$thirdPartyImportFilesPathRoot/$thirdPartyGroup.bzl")

  override def allThirdPartyImportTargetsFilesContent(): Set[String] =
    sourceFiles.filter(f => f._1.contains(thirdPartyImportFilesPathRoot + "/")).values.toSet

  override def allThirdPartyImportTargetsFiles(): Map[File, String] =
    sourceFiles
      .filter(f => f._1.matches(s"$thirdPartyImportFilesPathRoot\\/[^\\/]+.bzl"))
      .map(pair => (new File(pair._1), pair._2)).toMap

  override def overwriteBuildFile(packageName: String, content: String): Unit =
    sourceFiles.put(packageName + "/BUILD.bazel", content)

  override def thirdPartyOverrides(): ThirdPartyOverrides = overrides

  override def deleteAllThirdPartyImportTargetsFiles(): Unit = {
    allThirdPartyImportTargetsFiles().foreach { case (file: File, _: String) =>
      sourceFiles - file.getPath
    }
  }
}

object FakeLocalBazelWorkspace {
  val thirdPartyReposFilePath: String = "third_party.bzl"
  val thirdPartyImportFilesPathRoot: String = "third_party"
}
