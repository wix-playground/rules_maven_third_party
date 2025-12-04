package com.wix.build.sync.e2e

import com.wix.build.bazel.{DestinationPackage, ImportExternalTargetsFileReader, ThirdPartyPaths}
import com.wix.build.maven.Coordinates
import com.wix.build.translation.MavenToBazelTranslations._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.transport.RefSpec

import java.io.File
import java.nio.file.{Files, Paths}
import scala.util.Try

class FakeRemoteRepository() {
  private val destination = "third_party"
  val thirdPartyPaths = new ThirdPartyPaths(destination, s"$destination.bzl", DestinationPackage.resolveFromDestination(destination))

  import thirdPartyPaths._

  def initWithThirdPartyReposFileContent(content: String): FakeRemoteRepository = {
    writeThirdPartyReposFile(content)
    this
  }

  def commitThirdParties(thirdPartyFiles: Map[String, String]): Unit = {
    thirdPartyFiles foreach (f => writeThirdPartyFile(f._1, f._2))
  }

  private val DefaultRemote = "origin"
  private val DefaultBranch = "master"
  private val GitUserName = "builduser"
  private val GitUserEmail = "builduser@wix.com"

  private val remoteRepo = GitRepository.newRemote
  private val localClone = GitRepository.newLocalCloneOf(remoteRepo)

  def remoteURI: String = remoteRepo.pathAsString

  private def writeThirdPartyReposFile(content: String) = {
    val thirdPartyReposFile = Paths.get(localClone.pathAsString, thirdPartyReposFilePath)
    val git = localClone.git

    Files.writeString(thirdPartyReposFile, content)

    git.add()
      .addFilepattern(thirdPartyReposFile.toFile.getName)
      .call()

    git.commit()
      .setMessage("first commit")
      .setAuthor(GitUserName, GitUserEmail)
      .call()

    git.push()
      .setRemote(DefaultRemote)
      .setRefSpecs(new RefSpec(DefaultBranch))
      .call()
  }


  private def writeThirdPartyFile(fileName: String, content: String) = {
    val thirdPartyFile = Paths.get(localClone.pathAsString, thirdPartyReposFilePath, fileName)
    val git = localClone.git

    Files.writeString(thirdPartyFile, content)

    git.add()
      .addFilepattern(thirdPartyImportFilesPathRoot)
      .addFilepattern(thirdPartyFile.toFile.getName)
      .call()

    git.commit()
      .setMessage("blahhhh")
      .setAuthor(GitUserName, GitUserEmail)
      .call()

    git.push()
      .setRemote(DefaultRemote)
      .setRefSpecs(new RefSpec(DefaultBranch))
      .call()
  }

  def hasWorkspaceRuleFor(coordinates: Coordinates, branchName: String): Try[String] = {
    val importExternalRuleName = coordinates.workspaceRuleName
    val groupId = coordinates.groupIdForBazel
    updatedContentOfFileIn(branchName, relativePath = s"$thirdPartyImportFilesPathRoot/$groupId.bzl")
      .map { importExternalTargetsContent =>
        val maybeRule = ImportExternalTargetsFileReader(importExternalTargetsContent)
          .findCoordinatesByName(importExternalRuleName)
        maybeRule match {
          case Some(c) if c.coordinates == coordinates => "success"
          case _ => throw new RuntimeException(s"Could not find workspace rule for $coordinates in bazel remote repository")
        }
      }
  }


  def updatedContentOfFileIn(branchName: String, relativePath: String): Try[String] = {
    val git = localClone.git
    git.fetch().call()
    git.clean().setCleanDirectories(true).setForce(true).call()
    Try {
      git.reset().setRef(s"$DefaultRemote/$branchName").setMode(ResetType.HARD).call()
      val fullPath = Paths.get(localClone.pathAsString, relativePath)
      val file = fullPath.toFile
      if (!file.exists)
        throw new RuntimeException(s"path $relativePath does not exist")
      if (file.isDirectory)
        throw new RuntimeException(s"path $relativePath is a directory")

      Files.readString(fullPath)
    }
  }

}

case class Commit(username: String, email: String, message: String, changedFiles: Set[String])

case class GitRepository(path: File, git: Git) {
  def pathAsString: String = path.getPath
}

object GitRepository {
  def newRemote: GitRepository = {
    val remoteRepoDir = newDisposableDir("remote-dir")
    GitRepository(remoteRepoDir, Git.init()
      .setDirectory(remoteRepoDir)
      .setBare(true)
      .call())
  }

  def newLocalCloneOf(remoteRepo: GitRepository): GitRepository = {
    val localCloneDir = newDisposableDir("clone")
    GitRepository(localCloneDir, Git.cloneRepository()
      .setURI(remoteRepo.path.getPath)
      .setDirectory(localCloneDir)
      .call())
  }

  private def newDisposableDir(prefix: String): File = {
    val tmpDir = Files.createTempDirectory(prefix).toFile
    tmpDir.deleteOnExit()
    tmpDir
  }
}

object FakeRemoteRepository {
  def newBlankRepository(): FakeRemoteRepository = (new FakeRemoteRepository)
    .initWithThirdPartyReposFileContent(content = "")
}

