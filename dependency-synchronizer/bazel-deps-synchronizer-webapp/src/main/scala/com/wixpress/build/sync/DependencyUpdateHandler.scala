package com.wix.build.sync

import com.wix.build.bazel.{BazelRepository, ImportExternalLoadStatement}
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates}
import com.wix.ci.greyhound.events.BuildFinished
import com.wix.vi.githubtools.masterguard.enforceadmins.MasterEnforcer
import org.slf4j.LoggerFactory

class DependencyUpdateHandler(managedDependenciesUpdate: ManagedDependenciesUpdateHandler) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def handleBuildMessage(message: BuildFinished): Unit = {
    logger.info(s"I Got Build Message :$message")
    managedDependenciesUpdate.run(message.buildRunId)
  }
}

class ManagedDependenciesUpdateHandler(dependencyManagementArtifact: Coordinates,
                                       mavenRemoteRepositoryURL: List[String],
                                       dependenciesRemoteStorage: DependenciesRemoteStorage,
                                       importExternalLoadStatement: ImportExternalLoadStatement,
                                       gitSettings: GitSettings,
                                       masterEnforcer: MasterEnforcer) {

  def run(version: String): Unit = {
    // resolver has to be re-instantiated on each update, in order to get non-cached version of managed deps snapshot
    val resolver = new AetherMavenDependencyResolver(mavenRemoteRepositoryURL)
    val managedDepsBazelRepository: BazelRepository = ManagedBazelDepsClone.localCloneOfManagedDepsBazelRepository(gitSettings, masterEnforcer)

    val synchronizer = new BazelMavenManagedDepsSynchronizer(resolver, managedDepsBazelRepository, dependenciesRemoteStorage, importExternalLoadStatement)

    synchronizer.sync(
      dependencyManagementSource = dependencyManagementArtifact,
      branchName = version
    )
  }
}
