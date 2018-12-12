package com.wix.build.sync.fw

import better.files.File
import com.wix.build.bazel.{BazelRepository, FWThirdPartyPaths, NoPersistenceBazelRepository}
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates, Dependency, MavenScope}
import com.wix.build.sync._
import org.slf4j.LoggerFactory

object FWDependenciesSynchronizerCli extends App {
  private val log = LoggerFactory.getLogger(getClass)

  val parsedArgs = Args.parse(args)
  val fwArtifact = parsedArgs.fwArtifact
  val version = parsedArgs.version

  log.info("fwArtifact: " + fwArtifact)
  log.info("version: " + version)

  val mangaedDepsRepoLocalClone = parsedArgs.managedDepsRepoUrl

  log.info("mangaedDepsRepoLocalClone: " + mangaedDepsRepoLocalClone)

  val ManagedDependenciesArtifact = Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT")

  val remoteRepositoryURL = parsedArgs.mavenRemoteRepositoryURL
  val aetherResolver = new AetherMavenDependencyResolver(remoteRepositoryURL)

  val dependenciesRemoteStorage = new StaticDependenciesRemoteStorage(new MavenRepoRemoteStorage(remoteRepositoryURL))

  val bazelRepoWithManagedDependencies: BazelRepository= new NoPersistenceBazelRepository(File(mangaedDepsRepoLocalClone))

  private val synchronizer = new BazelMavenSynchronizer(aetherResolver, bazelRepoWithManagedDependencies, dependenciesRemoteStorage, FWThirdPartyPaths())

  private val dependenciesToSync = Set(toDependency(Coordinates.deserialize(fwArtifact).withVersion(version)))
  synchronizer.sync(ManagedDependenciesArtifact, dependenciesToSync)

  private def toDependency(coordinates: Coordinates): Dependency = {
    // scope here is of no importance as it is used on third_party and workspace only
    Dependency(coordinates, MavenScope.Compile)
  }
}

case class Args(mavenRemoteRepositoryURL: List[String],
                managedDepsRepoUrl: String,
                fwArtifact: String, version: String)

object Args {
  private val RepoFlag = "--binary-repo"
  private val fwArtifactFlag = "--fw_dep"
  private val versionFlag = "--version"
  //TODO: move to wix-maven-resolver module
  private val wixRepos = List(
    "http://repo.example.com:80/artifactory/libs-releases",
    "http://repo.example.com:80/artifactory/libs-snapshots")

  private val managedDepsRepoFlag = "--managed_deps_repo"

  val managedDepsRepoUrl = ""

  private val Usage =
    """Usage: DependencySynchronizer [--repo remoteRepoUrl] localRoot addedDependencies
      |addedDependencies format example: com.example:artifact-a:1.0.0,com.example:artifact-b:2.0.0
    """.stripMargin


  // consider moving to scopt.OptionParser!!!
  def parse(args: Array[String]): Args = args match {
    case Array(RepoFlag, remoteRepoUrl, managedDepsRepoFlag, managedDepsRepoUrl, repofwArtifactFlag, fwArtifact, versionFlag, version) => Args(List(remoteRepoUrl), managedDepsRepoUrl, fwArtifact, version)
    case Array(managedDepsRepoFlag, managedDepsRepoUrl, repofwArtifactFlag, fwArtifact, versionFlag, version) => Args(wixRepos, managedDepsRepoUrl, fwArtifact, version)
    case _ => throw new IllegalArgumentException(Usage)
  }
}
