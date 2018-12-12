package com.wix.build.sync.fw

import better.files.File
import com.wix.build.bazel._
import com.wix.build.maven._
import com.wix.build.sync._
import org.slf4j.LoggerFactory

object FWDependenciesSynchronizerCli extends App {
  private val log = LoggerFactory.getLogger(getClass)

  val parsedArgs = Args.parse(args)
  val fwArtifact = parsedArgs.fwArtifact

  log.info("fwArtifact: " + fwArtifact)

  val mangaedDepsRepoLocalClone = parsedArgs.managedDepsRepoUrl

  log.info("mangaedDepsRepoLocalClone: " + mangaedDepsRepoLocalClone)

  val ManagedDependenciesArtifact = Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT")

  val remoteRepositoryURL = parsedArgs.mavenRemoteRepositoryURL
  val aetherResolver = new AetherMavenDependencyResolver(remoteRepositoryURL)

  val dependenciesRemoteStorage = new StaticDependenciesRemoteStorage(new MavenRepoRemoteStorage(remoteRepositoryURL))

  val bazelRepoWithManagedDependencies: BazelRepository= new NoPersistenceBazelRepository(File(mangaedDepsRepoLocalClone))

  private val synchronizer = new BazelMavenSynchronizer(aetherResolver, bazelRepoWithManagedDependencies, dependenciesRemoteStorage, FWThirdPartyPaths())

  private val dependenciesToSync = Set(toDependency(Coordinates.deserialize(fwArtifact)))

  val managedDependenciesFromMaven = aetherResolver
    .managedDependenciesOf(ManagedDependenciesArtifact)
    .forceCompileScope

  log.info(s"Starting to calculate transitive dep closure for: $fwArtifact")

  private val nodes: Set[DependencyNode] = aetherResolver.dependencyClosureOf(dependenciesToSync, managedDependenciesFromMaven)

  log.info(s"Finished calculating transitive dep closure for: $fwArtifact")

  val nodesToUpdate = synchronizer.calcDepNodesToSync(ManagedDependenciesArtifact, nodes.map(_.baseDependency))
  val filteredNodes = filterNotThirdPartyArtifacts(nodesToUpdate)
  synchronizer.persist(filteredNodes)

  private def toDependency(coordinates: Coordinates): Dependency = {
    // scope here is of no importance as it is used on third_party and workspace only
    Dependency(coordinates, MavenScope.Compile)
  }

  private def filterNotThirdPartyArtifacts(nodesToUpdate: Set[DependencyNode]) = {
    val localWorkspace: BazelLocalWorkspace = bazelRepoWithManagedDependencies.localWorkspace("master")
    val thirdPartyDependenciesFromBazel = new BazelDependenciesReader(localWorkspace).allDependenciesAsMavenDependencies()

    nodesToUpdate.filterNot(n => thirdPartyDependenciesFromBazel.exists(t => t.equalsOnCoordinatesIgnoringVersion(n.baseDependency)))
  }
}

case class Args(mavenRemoteRepositoryURL: List[String],
                managedDepsRepoUrl: String,
                fwArtifact: String)

object Args {
  private val RepoFlag = "--binary-repo"
  //TODO: move to wix-maven-resolver module
  private val wixRepos = List(
    "http://repo.example.com:80/artifactory/libs-releases",
    "http://repo.example.com:80/artifactory/libs-snapshots")

  private val managedDepsRepoFlag = "--managed_deps_repo"

  val managedDepsRepoUrl = ""

  private val Usage =
    """Usage: DependencySynchronizer [--binary-repo remoteRepoUrl] --managed_deps_repo managedDepsRepoLocalPath fw-leaf-artifact
      |for example: --managed_deps_repo /path/to/managed/deps/repo com.wix.common:wix-framework-leaf:pom:1.0.0-SNAPSHOT
    """.stripMargin


  // consider moving to scopt.OptionParser!!!
  def parse(args: Array[String]): Args = args match {
    case Array(RepoFlag, remoteRepoUrl, managedDepsRepoFlag, managedDepsRepoUrl, fwArtifact) => Args(List(remoteRepoUrl), managedDepsRepoUrl, fwArtifact)
    case Array(managedDepsRepoFlag, managedDepsRepoUrl, fwArtifact) => Args(wixRepos, managedDepsRepoUrl, fwArtifact)
    case _ => throw new IllegalArgumentException(Usage)
  }
}
