package com.wix.build.sync

import better.files.File
import com.wix.build.sync.api.{BazelManagedDepsSyncEnded, ThirdPartyArtifact}
import com.wix.build.bazel.{BazelDependenciesReader, BazelRepository, ImportExternalLoadStatement}
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates}
import com.wix.ci.greyhound.events.BuildFinished
import com.wix.greyhound.DetailedProduceResult
import com.wix.greyhound.producer.builder.GreyhoundResilientProducer
import com.wix.vi.githubtools.masterguard.enforceadmins.MasterEnforcer
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class DependencyUpdateHandler(managedDependenciesUpdate: ManagedDependenciesUpdateHandler) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def handleBuildMessage(message: BuildFinished): Unit = {
    logger.info(s"Got Build message: $message")
    managedDependenciesUpdate.run(message.buildRunId)
  }
}

class ManagedDependenciesUpdateHandler(dependencyManagementArtifact: Coordinates,
                                       mavenRemoteRepositoryURL: List[String],
                                       dependenciesRemoteStorage: DependenciesRemoteStorage,
                                       importExternalLoadStatement: ImportExternalLoadStatement,
                                       gitSettings: GitSettings,
                                       masterEnforcer: MasterEnforcer) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private def tempManagedDepsClonePath(): File = {
    val tempDir = File.newTemporaryDirectory("managed_deps_clone")
    tempDir.toJava.deleteOnExit()
    tempDir
  }

  private def localCloneOfManagedDepsBazelRepository(git: GitSettings,
                                         masterEnforcer: MasterEnforcer): BazelRepository = {
    val authenticationWithToken = new GitAuthenticationWithToken(Option(git.githubToken).filterNot(_.isEmpty))

    new GitBazelRepository(GitRepo(git.managedDepsRepoURL),
      tempManagedDepsClonePath(),
      masterEnforcer,
      git.username,
      git.email
    )(authenticationWithToken)
  }

  def run(version: String) = {
    // resolver has to be re-instantiated on each update, in order to get non-cached version of managed deps snapshot
    val resolver = new AetherMavenDependencyResolver(mavenRemoteRepositoryURL)
    val managedDepsBazelRepository: BazelRepository = localCloneOfManagedDepsBazelRepository(gitSettings, masterEnforcer)

    val synchronizer = new BazelMavenSynchronizer(resolver, managedDepsBazelRepository, dependenciesRemoteStorage, importExternalLoadStatement)

    val managedDependencies = resolver.managedDependenciesOf(dependencyManagementArtifact)

    logger.info(s"retrieved ${managedDependencies.size} managed depednencies from $dependencyManagementArtifact")

    synchronizer.sync(
      dependencyManagementSource = dependencyManagementArtifact,
      dependencies = managedDependencies,
      branchName = version
    )
  }
}



class ManagedDepsSyncFinished(managedDepsBazelRepository: BazelRepository,syncEndedProducer : GreyhoundResilientProducer){
  //consumed by LabelDex! so what to do? we don't want to commit to master automatically anymore, so this job can not be the one to tell LabelDex anything
  // i.e we have currently coupled the calculation with committing it's change into one single job
  //do we have some other way of making labeldex scan by commits to master?
  def publishEvent(): Future[DetailedProduceResult] = {
    val thirdPartyManagedArtifacts = readManagedArtifacts()
    syncEndedProducer.produce(BazelManagedDepsSyncEnded(thirdPartyManagedArtifacts))
  }

  private def readManagedArtifacts() : Set[ThirdPartyArtifact] = {
    val managedDepsRepoReader = new BazelDependenciesReader(managedDepsBazelRepository.resetAndCheckoutMaster())
    managedDepsRepoReader.allDependenciesAsMavenDependencyNodes()
      .map(d=>ThirdPartyArtifact(d.baseDependency.coordinates.groupId,
        d.baseDependency.coordinates.artifactId,
        d.baseDependency.coordinates.version,
        d.baseDependency.coordinates.packaging.value,
        d.baseDependency.coordinates.classifier,
        None))
  }
}

