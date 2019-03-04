package com.wix.build.sync.e2e

import better.files.File
import com.wix.build.bazel.FakeLocalBazelWorkspace.thirdPartyReposFilePath
import com.wix.build.sync.{GitAuthenticationWithToken, GitBazelRepository, GitRepo}
import com.wix.vi.githubtools.masterguard.enforceadmins.MasterEnforcer
import org.eclipse.jgit.api.Git
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class GitBazelRepositoryIT extends SpecificationWithJUnit {
  implicit val gitAuthnetication = new GitAuthenticationWithToken(tokenApi = None)

  "GitBazelRepository" should {

    "reset whatever was in given path to clone of given git URL" in new fakeRemoteRepositoryWithEmptyThirdPartyRepos {
      val someLocalPath = aRandomTempDirectory
      someLocalPath.createChild("some-file.txt").overwrite("some content")

      new GitBazelRepository(fakeGitRepo, someLocalPath, fakeMasterEnforcer)

      someLocalPath.list.toList must contain(exactly(someLocalPath / thirdPartyReposFilePath, someLocalPath / ".git"))
    }

    "create local path (including parents) even if they did not exit beforehand" in new fakeRemoteRepositoryWithEmptyThirdPartyRepos {
      val nonExistingLocalPath = aRandomTempDirectory / "plus" / "some" / "new" / "subdirectories"

      new GitBazelRepository(fakeGitRepo, nonExistingLocalPath, fakeMasterEnforcer)

      eventually {
        (nonExistingLocalPath / thirdPartyReposFilePath).exists aka "third party repos file was checked out" must beTrue
      }
    }

    "return valid bazel local third party repos content" in {
      val thirdPartyReposFileContent = "some third party repos file content"
      val fakeRemoteRepository = aFakeRemoteRepoWithThirdPartyReposFile(thirdPartyReposFileContent)
      val fakeGitRepo = GitRepo(fakeRemoteRepository.remoteURI, "someOrg", "someRepoName")
      val gitBazelRepository = new GitBazelRepository(fakeGitRepo, aRandomTempDirectory, new SpyMasterEnforcer)

      val localWorkspace = gitBazelRepository.localWorkspace()

      localWorkspace.thirdPartyReposFileContent() mustEqual thirdPartyReposFileContent
    }

    "return valid bazel local third party repos content for some-branch" in new exitingThirdPartyRepo{
      val localWorkspace = gitBazelRepository.localWorkspace()

      localWorkspace.thirdPartyReposFileContent() mustEqual thirdPartyReposFileContent
    }

    "persist file change to remote git repository" in new fakeRemoteRepositoryWithEmptyThirdPartyRepos {
      val someLocalPath = File.newTemporaryDirectory("clone")
      val gitBazelRepository = new GitBazelRepository(fakeGitRepo, someLocalPath, fakeMasterEnforcer)

      val fileName = "some-file.txt"
      val content = "some content"
      someLocalPath.createChild(fileName).overwrite(content)

      val branchName = "some-branch"
      gitBazelRepository.persist(branchName, Set(fileName), "some message")

      fakeRemoteRepository.updatedContentOfFileIn(branchName, fileName) must beSuccessfulTry(content)
    }

    "overwrite any file in target branch with the persist content" in new fakeRemoteRepositoryWithEmptyThirdPartyRepos {
      val someLocalPath = File.newTemporaryDirectory("clone")
      val gitBazelRepository = new GitBazelRepository(fakeGitRepo, someLocalPath, fakeMasterEnforcer)

      val branchName = "some-branch"

      gitBazelRepository.localWorkspace().overwriteThirdPartyReposFile("old-content")
      gitBazelRepository.persist(branchName, Set(thirdPartyReposFilePath), "some message")

      val newContent = "new-content"
      gitBazelRepository.localWorkspace().overwriteThirdPartyReposFile(newContent)
      gitBazelRepository.persist(branchName, Set(thirdPartyReposFilePath), "some message")

      fakeRemoteRepository.updatedContentOfFileIn(branchName, thirdPartyReposFilePath) must beSuccessfulTry(newContent)
    }

    "persist short lived branch with new content" in new fakeRemoteRepositoryWithEmptyThirdPartyRepos {
      val someLocalPath = File.newTemporaryDirectory("clone")
      val gitBazelRepository = new GitBazelRepository(fakeGitRepo, someLocalPath, fakeMasterEnforcer)

      val branchName = "some-branch"

      val newContent = "new-content"
      gitBazelRepository.localWorkspace().overwriteThirdPartyReposFile(newContent)
      gitBazelRepository.persist(branchName, Set(thirdPartyReposFilePath), "some message")

      fakeRemoteRepository.updatedContentOfFileIn(branchName, thirdPartyReposFilePath) must beSuccessfulTry(newContent)
    }

    "persist with commit message with given username and email that will be visible on remote repository" in new fakeRemoteRepositoryWithEmptyThirdPartyRepos {
      val someLocalPath = File.newTemporaryDirectory("clone")
      val username = "someuser"
      val email = "some@email.com"
      val gitBazelRepository = new GitBazelRepository(fakeGitRepo, someLocalPath, fakeMasterEnforcer, username, email)
      val fileName = "some-file.txt"
      someLocalPath.createChild(fileName).overwrite("some content")
      val someMessage = "some message"

      private val branchName = "some-branch"
      gitBazelRepository.persist(branchName, Set(fileName), someMessage)
      val expectedCommit = Commit(
        username = username,
        email = email,
        message = someMessage,
        changedFiles = Set(fileName)
      )

      fakeRemoteRepository.allCommitsForBranch(branchName) must contain(expectedCommit)
      fakeMasterEnforcer.calledForOrgAndRepo("someOrg", "someRepoName") must beTrue
    }

    "not throw exception for new localWorkspace for some branch when there are old conflicting changes" in new noConflictCtx {
      val localGitPath = File.newTemporaryDirectory("clone")
      val gitBazelRepository = new GitBazelRepository(fakeGitRepo, localGitPath, new SpyMasterEnforcer)

      addFileAndCommit(branchName, fileName, "old content")(localGitPath)
      addFile(DefaultBranch, fileName, "new content")(localGitPath)

      gitBazelRepository.localWorkspace()

      checkoutBranch(branchName)(localGitPath) must not throwA[Exception]()

    }
  }

  trait fakeRemoteRepositoryWithEmptyThirdPartyRepos extends Scope {
    val fakeMasterEnforcer: SpyMasterEnforcer = new SpyMasterEnforcer

    val fakeRemoteRepository = new FakeRemoteRepository
    val fakeGitRepo = new GitRepo(fakeRemoteRepository.remoteURI, "someOrg", "someRepoName")
    fakeRemoteRepository.initWithThirdPartyReposFileContent("")
  }

  trait noConflictCtx extends fakeRemoteRepositoryWithEmptyThirdPartyRepos {
    val DefaultBranch = "master"
    val branchName = "some-branch"
    val fileName = "some-file.txt"
  }

  trait exitingThirdPartyRepo extends Scope {
    val thirdPartyReposFileContent = "some third party repos file content"
    val fakeRemoteRepository = aFakeRemoteRepoWithThirdPartyReposFile(thirdPartyReposFileContent)
    val fakeGitRepo = new GitRepo(fakeRemoteRepository.remoteURI, "someOrg", "someRepoName")

    val gitBazelRepository = new GitBazelRepository(fakeGitRepo, aRandomTempDirectory, new SpyMasterEnforcer)
  }

  private def aRandomTempDirectory = {
    val dir = File.newTemporaryDirectory("local-clone")
    dir.toJava.deleteOnExit()
    dir
  }

  private def aFakeRemoteRepoWithThirdPartyReposFile(thirdPartyReposFileContent: String) =
    (new FakeRemoteRepository).initWithThirdPartyReposFileContent(thirdPartyReposFileContent)

  class SpyMasterEnforcer extends MasterEnforcer {

    var calledRepos:scala.collection.immutable.Set[(String, String)] = Set()

    override def enforceAdmins[T](org: String, repo: String, f: => T): Unit = {
      f
      calledRepos += ((org, repo))
      println(s"Talya - " + calledRepos.toList)
    }

    def calledForOrgAndRepo(org: String, repo: String) = calledRepos.contains((org, repo))
  }

  private def checkoutBranch(branchName: String)(checkoutDir: File) = {
    val git: Git = Git.open(checkoutDir.toJava)

    git.checkout()
      .setCreateBranch(false)
      .setName(branchName)
      .call()
  }

  private def addFileAndCommit(branchName: String, newFile: String, content: String)(checkoutDir: File) = {
    val git: Git = Git.open(checkoutDir.toJava)

    git.checkout()
      .setCreateBranch(true)
      .setName(branchName)
      .call()

    checkoutDir.createChild(newFile).overwrite(content)
    val gitAdd = git.add()
    Set(newFile).foreach(gitAdd.addFilepattern)
    gitAdd.call()

    git.commit()
      .setMessage("bla")
      .setAuthor("bla", "bla@foo.nb")
      .call()
  }

  private def addFile(branchName: String, fileName: String, content: String)(checkoutDir: File) = {
    val git: Git = Git.open(checkoutDir.toJava)
    git.checkout()
      .setName(branchName)
      .call()

    checkoutDir.createChild(fileName).overwrite(content)
    val gitNewAdd = git.add()

    Set(fileName).foreach(gitNewAdd.addFilepattern)
    gitNewAdd.call()
  }
}
