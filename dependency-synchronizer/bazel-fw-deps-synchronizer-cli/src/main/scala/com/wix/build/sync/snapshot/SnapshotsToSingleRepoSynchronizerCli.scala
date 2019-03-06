package com.wix.build.sync.snapshot

import java.nio.file.Path

import better.files.File
import com.wix.build.maven.analysis.{MavenSourceModules, RepoProvidedDeps}
import com.wix.build.sync.SourceModulesOverridesReaderDeleteOnPhase2
import com.wix.build.bazel._
import com.wix.build.maven._
import com.wix.build.sync._
import org.slf4j.LoggerFactory

//TODO - this is the cli run by jenkins (via groovy scripts)
//either rename these classes and the module name itself,
//or better yet - consolidate this with the DependencySynchronizerCli
object SnapshotsToSingleRepoSynchronizerCli extends App {

  private val log = LoggerFactory.getLogger(getClass)

  val config = SnapshotsToSingleRepoSynchronizerCliConfig.parse(args)
  val snapshotModules = config.fwArtifact

  log.info("snapshot modules: " + snapshotModules)

  val targetRepoLocalClone = config.targetRepoUrl

  log.info("targetRepoLocalClone: " + targetRepoLocalClone)

  val managedDepsRepoLocalClone = config.managedDepsRepoUrl

  log.info("managedDepsRepoLocalClone: " + managedDepsRepoLocalClone)

  val ManagedDependenciesArtifact = Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT")

  val remoteRepositoryURL = config.mavenRemoteRepositoryURL
  val aetherResolver = new AetherMavenDependencyResolver(remoteRepositoryURL)

  val dependenciesRemoteStorage = new StaticDependenciesRemoteStorage(new MavenRepoRemoteStorage(remoteRepositoryURL))

  val targetBazelRepo: BazelRepository= new NoPersistenceBazelRepository(File(targetRepoLocalClone))
  val managedDepsBazelRepo: BazelRepository= new NoPersistenceBazelRepository(File(managedDepsRepoLocalClone))

  log.info("Reading maven modules of target repo in order to include potential source dependencies (for phase 1 only)...")
  private val repoPath: Path = File(targetRepoLocalClone).path
  val mavenModules = new MavenSourceModules(repoPath, SourceModulesOverridesReaderDeleteOnPhase2.from(repoPath)).modules()

  val neverLinkResolver = NeverLinkResolver(RepoProvidedDeps(mavenModules).repoProvidedArtifacts)
  val calculator = new UserAddedDepsDiffCalculator(targetBazelRepo,
    managedDepsBazelRepo,
    ManagedDependenciesArtifact,
    aetherResolver,
    dependenciesRemoteStorage,
    mavenModules
  )
  val synchronizer = new UserAddedDepsDiffSynchronizer(calculator, DefaultDiffWriter(targetBazelRepo, neverLinkResolver))

  val dependenciesToSync = snapshotModules.split(",").map(a => toDependency(Coordinates.deserialize(a))).toSet

  synchronizer.syncThirdParties(dependenciesToSync)

  private def toDependency(coordinates: Coordinates): Dependency = {
    // scope here is of no importance as it is used on third_party and workspace only
    Dependency(coordinates, MavenScope.Compile)
  }
}


