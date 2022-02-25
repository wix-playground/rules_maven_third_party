package com.wix.build.sync.e2e

import better.files.File
import com.gitblit.utils.JGitUtils
import com.wix.build.bazel.{ImportExternalTargetsFileReader, ThirdPartyPaths, ValidatedCoordinates}
import com.wix.build.maven.Coordinates
import com.wix.build.maven.translation.MavenToBazelTranslations._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.RefSpec

import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.util.Try

class FakeRemoteRepository() {
  val thirdPartyPaths = new ThirdPartyPaths("third_party")

  import thirdPartyPaths._

  def allCommitsForBranch(branchName: String): List[Commit] = remoteRepo.git.log()
    .add(remoteRepo.git.getRepository.resolve(branchName))
    .call()
    .asScala
    .map(_.asCaseClass)
    .toList

  def initWithThirdPartyReposFileContent(content: String): FakeRemoteRepository = {
    writeThirdPartyReposFile(content)
    this
  }

  def commitThirdParties(thirdPartyFiles: Map[String, String]) = {
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
    val thirdPartyReposFile = localClone.path.createChild(thirdPartyReposFilePath)
    val git = localClone.git
    thirdPartyReposFile.overwrite(content)
    git.add()
      .addFilepattern(thirdPartyReposFile.name)
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
    val thirdPartyFile = localClone.path.createChild(thirdPartyImportFilesPathRoot, true).createChild(fileName)
    val git = localClone.git
    thirdPartyFile.overwrite(content)
    git.add()
      .addFilepattern(thirdPartyImportFilesPathRoot)
      .addFilepattern(thirdPartyFile.name)
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

  implicit class commitExtension(revCommit: RevCommit) {
    private def username = revCommit.getAuthorIdent.getName

    private def email = revCommit.getAuthorIdent.getEmailAddress

    private def message = revCommit.getFullMessage

    private def changedFiles = JGitUtils.getFilesInCommit(remoteRepo.git.getRepository, revCommit, false)
      .asScala.map(_.path).toSet

    def asCaseClass: Commit = Commit(username, email, message, changedFiles)

  }


  def hasWorkspaceRuleFor(coordinates: Coordinates, branchName: String): Try[String] = {
    val importExternalRuleName = coordinates.workspaceRuleName
    val groupId = coordinates.groupIdForBazel
    updatedContentOfFileIn(branchName, s"$thirdPartyImportFilesPathRoot/$groupId.bzl").map((importExternalTargetsContent) => {
      val maybeRule: Option[ValidatedCoordinates] = ImportExternalTargetsFileReader(importExternalTargetsContent).findCoordinatesByName(importExternalRuleName)
      maybeRule match {
        case Some(c) if c.coordinates == coordinates => "success"
        case _ => throw new RuntimeException(s"Could not find workspace rule for $coordinates in bazel remote repository")
      }
    })
  }


  def updatedContentOfFileIn(branchName: String, relativePath: String): Try[String] = {
    val git = localClone.git
    git.fetch().call()
    git.clean().setCleanDirectories(true).setForce(true).call()
    Try {
      git.reset().setRef(s"$DefaultRemote/$branchName").setMode(ResetType.HARD).call()
      val fullPath = localClone.path / relativePath
      if (!fullPath.exists)
        throw new RuntimeException(s"path $relativePath does not exist")
      if (fullPath.isDirectory)
        throw new RuntimeException(s"path $relativePath is a directory")

      fullPath.contentAsString(StandardCharsets.UTF_8)
    }
  }

}

case class Commit(username: String, email: String, message: String, changedFiles: Set[String])

case class GitRepository(path: File, git: Git) {
  def pathAsString: String = path.pathAsString
}

object GitRepository {
  def newRemote: GitRepository = {
    val remoteRepoDir = newDisposableDir("remote-dir")
    GitRepository(remoteRepoDir, Git.init()
      .setDirectory(remoteRepoDir.toJava)
      .setBare(true)
      .call())
  }

  def newLocalCloneOf(remoteRepo: GitRepository): GitRepository = {
    val localCloneDir = newDisposableDir("clone")
    GitRepository(localCloneDir, Git.cloneRepository()
      .setURI(remoteRepo.path.pathAsString)
      .setDirectory(localCloneDir.path.toFile)
      .call())
  }

  private def newDisposableDir(prefix: String): File = {
    val tmpDir = File.newTemporaryDirectory(prefix)
    tmpDir.toJava.deleteOnExit()
    tmpDir
  }
}

object FakeRemoteRepository {
  def newBlankRepository(): FakeRemoteRepository = (new FakeRemoteRepository).initWithThirdPartyReposFileContent("")
}

