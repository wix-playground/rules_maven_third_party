package com.wix.build.sync

import com.wix.build.bazel._
import com.wix.build.maven._
import com.wix.build.sync.ArtifactoryRemoteStorage.decorateNodesWithChecksum
import com.wix.build.sync.BazelMavenManagedDepsSynchronizer._
import org.slf4j.LoggerFactory

class BazelMavenManagedDepsSynchronizer(mavenDependencyResolver: MavenDependencyResolver,
                                        managedDepsBazelRepository: BazelRepository,
                                        dependenciesRemoteStorage: DependenciesRemoteStorage,
                                        importExternalLoadStatement: ImportExternalLoadStatement) {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val persister = new BazelDependenciesPersister(ManagedDepsUpdateCommitMsg, managedDepsBazelRepository)

  def sync(dependencyManagementSource: Coordinates, branchName: String): Unit = {
    val managedDependenciesFromMaven = mavenDependencyResolver.managedDependenciesOf(dependencyManagementSource).forceCompileScope
    logger.info(s"calculated ${managedDependenciesFromMaven.size} managed dependencies from the pom.")

    val dependenciesToUpdateWithChecksums = calcDepsNodesToSync(managedDependenciesFromMaven)
    persist(dependenciesToUpdateWithChecksums, branchName)
  }

  private def calcDepsNodesToSync(managedDependencies: List[Dependency]) = {
    val dependenciesToUpdate = mavenDependencyResolver.dependencyClosureOf(managedDependencies, managedDependencies)
    logger.info(s"syncing ${dependenciesToUpdate.size} dependencies which are the closure of the ${managedDependencies.size} managed dependencies")

    if (dependenciesToUpdate.isEmpty)
      Set[BazelDependencyNode]()
    else {
      logger.info("started fetching sha256 checksums for 3rd party dependencies from artifactory...")
      val nodes = decorateNodesWithChecksum(dependenciesToUpdate)(dependenciesRemoteStorage)
      logger.info("completed fetching sha256 checksums.")
      nodes
    }
  }

  def persist(dependenciesToUpdate: Set[BazelDependencyNode], branchName: String): Unit = {
    if (dependenciesToUpdate.nonEmpty) {
      val localCopy = managedDepsBazelRepository.resetAndCheckoutMaster()

      new BazelDependenciesWriter(
        localCopy,
        importExternalLoadStatement = importExternalLoadStatement
      ).writeDependencies(dependenciesToUpdate)

      persister.persistWithMessage(
        Some(branchName),
        asPr = true
      )
    }
  }
}

object BazelMavenManagedDepsSynchronizer {
  val ManagedDepsUpdateCommitMsg = "Automatic update of 'third_party' import files for managed deps based on 3rd_party pom changes"
}
