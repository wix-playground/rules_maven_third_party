package com.wix.build.sync.core

import com.wix.build.bazel.{BazelDependenciesWriter, FileSystemBazelLocalWorkspace, ImportExternalLoadStatement, ThirdPartyPaths}
import com.wix.build.maven._
import com.wix.build.sync.ArtifactoryRemoteStorage.decorateNodesWithChecksum
import com.wix.build.sync.DependenciesRemoteStorage
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ManagedDependenciesSynchronizer(mavenDependencyResolver: MavenDependencyResolver,
                                      managedDependenciesRepoPath: Path,
                                      destination: String,
                                      dependenciesRemoteStorage: DependenciesRemoteStorage,
                                      managedDependencies: List[Dependency],
                                      importExternalLoadStatement: ImportExternalLoadStatement)
  extends DependenciesSynchronizer {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def sync(currentRepoDependencies: List[Dependency]): Unit = {

    val dependenciesToUpdateWithChecksums = calcManagedNodesToSync(
      currentRepoDependencies,
      managedDependencies
    )

    new BazelDependenciesWriter(
      new FileSystemBazelLocalWorkspace(managedDependenciesRepoPath.toFile, new ThirdPartyPaths(destination)),
      importExternalLoadStatement = importExternalLoadStatement
    ).writeDependencies(dependenciesToUpdateWithChecksums)
  }

  private def calcManagedNodesToSync(currentRepoDependencies: List[Dependency],
                                     managedDependencies: List[Dependency]): Set[BazelDependencyNode] = {

    val naiveDependencyGraph = mavenDependencyResolver.dependencyClosureOf(currentRepoDependencies, managedDependencies)

    logger.info(s"syncing ${naiveDependencyGraph.size} dependencies which are the closure of the ${managedDependencies.size} managed dependencies")

    if (naiveDependencyGraph.isEmpty)
      Set[BazelDependencyNode]()
    else {
      logger.info("started fetching sha256 checksums for 3rd party dependencies from artifactory...")
      val nodes = decorateNodesWithChecksum(naiveDependencyGraph)(dependenciesRemoteStorage)
      logger.info("completed fetching sha256 checksums.")

      DependencyConfigAugmenter.augment(nodes, managedDependencies)
    }
  }

}