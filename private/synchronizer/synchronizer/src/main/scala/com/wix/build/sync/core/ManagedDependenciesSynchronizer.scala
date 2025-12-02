package com.wix.build.sync.core

import com.wix.build.bazel.{BazelDependenciesWriter, DestinationPackage, FileSystemBazelLocalWorkspace, ImportExternalLoadStatement, ThirdPartyPaths}
import com.wix.build.maven._
import com.wix.build.sync.ArtifactoryRemoteStorage.decorateNodesWithChecksum
import com.wix.build.sync.DependenciesRemoteStorage
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ManagedDependenciesSynchronizer(mavenDependencyResolver: MavenDependencyResolver,
                                      managedDependenciesRepoPath: Path,
                                      destination: String,
                                      aggregatorPath: Option[String],
                                      destinationPackage: DestinationPackage,
                                      dependenciesRemoteStorage: DependenciesRemoteStorage,
                                      managedDependencies: List[Dependency],
                                      importExternalLoadStatement: ImportExternalLoadStatement,
                                      failOnMissingArtifacts: Boolean,
                                      failOnSnapshotVersions: Boolean)
  extends DependenciesSynchronizer {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def sync(currentRepoDependencies: List[Dependency]): Unit = {

    val dependenciesToUpdateWithChecksums = calcManagedNodesToSync(
      currentRepoDependencies,
      managedDependencies,
      failOnSnapshotVersions
    )

    new BazelDependenciesWriter(
      new FileSystemBazelLocalWorkspace(
        managedDependenciesRepoPath.toFile,
        new ThirdPartyPaths(destination, aggregatorPath, destinationPackage),
      ),
      importExternalLoadStatement = importExternalLoadStatement,
      failOnMissingArtifacts = failOnMissingArtifacts
    ).writeDependencies(dependenciesToUpdateWithChecksums)
  }

  private def calcManagedNodesToSync(currentRepoDependencies: List[Dependency],
                                     managedDependencies: List[Dependency],
                                     failOnSnapshotVersions: Boolean): Set[BazelDependencyNode] = {

    val naiveDependencyGraph = mavenDependencyResolver.dependencyClosureOf(currentRepoDependencies, managedDependencies)

    if (failOnSnapshotVersions) {
      val snapshotDeps = naiveDependencyGraph.filter(_.isSnapshot)
      if (snapshotDeps.nonEmpty) {
        val snapshotDepsStr = snapshotDeps
          .map(_.baseDependency.coordinates)
          .map(d => s"${d.groupId}:${d.artifactId}:${d.version}").mkString("\n")

        throw new IllegalStateException(s"Snapshot dependencies are not supported, found:\n$snapshotDepsStr")
      }
    }

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