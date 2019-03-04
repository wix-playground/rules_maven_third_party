package com.wix.build.sync

import java.util.regex.Pattern

import better.files.File
import com.jcraft.jsch.Session
import com.wix.build.bazel.{BazelLocalWorkspace, BazelRepository, FileSystemBazelLocalWorkspace}
import com.wix.vi.githubtools.masterguard.enforceadmins.MasterEnforcer
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api.{Git, TransportCommand}
import org.eclipse.jgit.transport.{JschConfigSessionFactory, SshTransport, _}
import org.slf4j.LoggerFactory

class GitBazelRepository(
                          gitRepo: GitRepo,
                          checkoutDir: File,
                          masterEnforcer: MasterEnforcer,
                          username: String = "blah",
                          email: String = "notanadmin@wix.com")
                          (implicit authentication: GitAuthentication) extends BazelRepository {

  private val log = LoggerFactory.getLogger(getClass)

  private val DefaultRemote = "origin"
  private val DefaultBranch = "master"

  init()

  private def init(): Unit = {
    checkoutDir.delete(swallowIOExceptions = true).createDirectories()
    val git = authentication.set(Git.cloneRepository())
      .setURI(gitRepo.gitURL)
      .setDirectory(checkoutDir.toJava)
      .call()
    git.close()
  }

  override def localWorkspace(): BazelLocalWorkspace = {
    cleanAndUpdateLocalRepo()
    new FileSystemBazelLocalWorkspace(checkoutDir)
  }

  override def persist(branchName: String, changedFilePaths: Set[String], message: String): Unit = {
    withLocalGit(git => {
      checkoutNewBranch(git, branchName)
      addFilesAndCommit(git, changedFilePaths, message)
      pushToRemote(git, branchName)
    })
  }

  private def cleanAndUpdateLocalRepo() = {
    withLocalGit(git => {
      authentication.set(git.fetch())
        .call()

      git.clean()
        .setCleanDirectories(true)
        .setForce(true)
        .call()

      git.reset()
        .setRef(s"$DefaultRemote/$DefaultBranch")
        .setMode(ResetType.HARD)
        .call()
    })
  }

  private def checkoutNewBranch(git: Git, branchName: String) = {
    git.checkout()
      .setName(DefaultBranch)
      .call()

    if (branchName != DefaultBranch) {
      git.branchDelete()
        .setForce(true)
        .setBranchNames(branchName)
        .call()

      git.checkout()
        .setCreateBranch(true)
        .setName(branchName)
        .call()
    }
  }

  private def addFilesAndCommit(git: Git, changedFilePaths: Set[String], message: String) = {
    val gitAdd = git.add()
    changedFilePaths.foreach(gitAdd.addFilepattern)
    gitAdd.call()

    git.commit()
      .setMessage(message)
      .setAuthor(username, email)
      .call()
  }

  private def pushToRemote(git: Git, branchName: String) = {
    log.info(s"pushing to ${gitRepo.gitURL}, branch: $branchName")
    masterEnforcer.enforceAdmins(gitRepo.org, gitRepo.repoName, {
      authentication.set(git.push())
        .setRemote(DefaultRemote)
        .setRefSpecs(new RefSpec(branchName))
        .setForce(true)
        .call()
    })
  }

  private def withLocalGit[T](f: Git => T): T = {
    val git = Git.open(checkoutDir.toJava)
    try {
      f(git)
    }
    finally {
      git.close()
    }
  }
}

case class GitRepo(gitURL: String, org: String, repoName: String)

object GitRepo {
  def apply(gitURL: String): GitRepo = {
    val pattern = Pattern.compile("https://github.com/(.+?)/(.+?).git")
    val matcher = pattern.matcher(gitURL)

    matcher.find() match {
      case true => GitRepo(gitURL, matcher.group(1), matcher.group(2))
      case false => GitRepo(gitURL, "defaultForNonValidUrl", "defaultForNonValidUrl")
    }
  }
}

trait GitAuthentication {
  def set[T <: TransportCommand[T, _]](command: T): T
}

class GitAuthenticationWithToken(tokenApi: Option[String] = None) extends GitAuthentication {
  override def set[T <: TransportCommand[T, _]](command: T): T = {
    tokenApi.foreach(token => command.setCredentialsProvider(credentialsProviderFor(token)))
    command
  }
  private def credentialsProviderFor(token: String) = new UsernamePasswordCredentialsProvider(token, "")
}

object GitAuthenticationWithSsh extends GitAuthentication {
  override def set[T <: TransportCommand[T, _]](command: T): T = {
    val sshSessionFactory = new JschConfigSessionFactory() {
      override protected def configure(host: OpenSshConfig.Host, session: Session): Unit = {}
    }

    command.setTransportConfigCallback((transport: Transport) => {
      val sshTransport = transport.asInstanceOf[SshTransport]
      sshTransport.setSshSessionFactory(sshSessionFactory)
    })
  }
}
