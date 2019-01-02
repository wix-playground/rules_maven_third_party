package com.wix.build.sync.fw

import better.files.File
import com.wix.build.bazel._
import com.wix.build.maven._
import com.wix.build.sync._
import org.slf4j.LoggerFactory

object FWDependenciesSynchronizerCli extends App {
  final val fwLeaf = Coordinates("com.wix.common", "wix-framework-leaf", "1.0.0", Packaging("pom"))

  private val log = LoggerFactory.getLogger(getClass)

  val config = Config.parse(args)
  val fwArtifact = config.fwArtifact

  log.info("fwArtifact: " + fwArtifact)

  val mangaedDepsRepoLocalClone = config.managedDepsRepoUrl

  log.info("mangaedDepsRepoLocalClone: " + mangaedDepsRepoLocalClone)

  val ManagedDependenciesArtifact = Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT")

  val remoteRepositoryURL = config.mavenRemoteRepositoryURL
  val aetherResolver = new AetherMavenDependencyResolver(remoteRepositoryURL)

  val dependenciesRemoteStorage = new StaticDependenciesRemoteStorage(new MavenRepoRemoteStorage(remoteRepositoryURL))

  val bazelRepoWithManagedDependencies: BazelRepository= new NoPersistenceBazelRepository(File(mangaedDepsRepoLocalClone))

  private val synchronizer = new BazelMavenSynchronizer(aetherResolver, bazelRepoWithManagedDependencies, dependenciesRemoteStorage, FWThirdPartyPaths())

  private val dependenciesToSync = Set(toDependency(Coordinates.deserialize(fwArtifact)))

  val managedDependenciesFromMaven = aetherResolver
    .managedDependenciesOf(ManagedDependenciesArtifact)
    .forceCompileScope

  private val deps = config.additionalDeps.map(artifact => toDependency(Coordinates.deserialize(artifact))).toSet
  log.info("additional dependencies to synchronize: " + deps)

  log.info(s"Starting to calculate transitive dep closure for: $fwArtifact and the additional deps")

  private val nodes: Set[DependencyNode] = aetherResolver.dependencyClosureOf(dependenciesToSync ++ deps, managedDependenciesFromMaven)

  log.info(s"Finished calculating transitive dep closure for: $fwArtifact")

  log.info(s"raw fw transitive nodes list: ${nodes.map(_.baseDependency.coordinates).mkString("\n")}")

  log.info(s"\n\n\n%%%%%%%%%%%%\n\n\n")


  val nodesToUpdate = synchronizer.calcDepNodesToSync(ManagedDependenciesArtifact, nodes.map(_.baseDependency))
  log.info(s"==============")

  log.info(s"unique diff fw transitive nodes list: ${nodesToUpdate.map(_.baseDependency.coordinates).mkString("\n")}")
  log.info(s"\n\n\n##############\n\n\n")

  val filteredNodes = filterNotThirdPartyArtifacts(nodesToUpdate)

  log.info(s"filtered fw transitive nodes list: ${nodesToUpdate.map(_.baseDependency.coordinates).mkString("\n")}")

  synchronizer.persist(filteredNodes)

  private def toDependency(coordinates: Coordinates): Dependency = {
    // scope here is of no importance as it is used on third_party and workspace only
    Dependency(coordinates, MavenScope.Compile)
  }

  private def filterNotThirdPartyArtifacts(nodesToUpdate: Set[DependencyNode]) = {
    val localWorkspace: BazelLocalWorkspace = bazelRepoWithManagedDependencies.localWorkspace("master")
    val thirdPartyDependenciesFromBazel = new BazelDependenciesReader(localWorkspace).allDependenciesAsMavenDependencies()

    nodesToUpdate.filterNot(n => thirdPartyDependenciesFromBazel.exists(t => t.equalsOnCoordinatesIgnoringVersion(n.baseDependency))).filterNot(n => {
      n.baseDependency.coordinates.equalsOnGroupIdAndArtifactId(fwLeaf)
    })
  }
}


