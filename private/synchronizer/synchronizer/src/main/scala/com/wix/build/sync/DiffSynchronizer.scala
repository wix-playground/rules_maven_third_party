package com.wix.build.sync

import com.wix.build.bazel._
import com.wix.build.maven._
import com.wix.build.sync.ArtifactoryRemoteStorage._
import com.wix.build.git.GitAdder
import org.slf4j.LoggerFactory

case class DiffSynchronizer(maybeBazelRepositoryWithManagedDependencies: Option[BazelRepository],
                            targetRepository: BazelRepository, resolver: MavenDependencyResolver,
                            dependenciesRemoteStorage: DependenciesRemoteStorage,
                            neverLinkResolver: NeverLinkResolver = NeverLinkResolver(),
                            importExternalLoadStatement: ImportExternalLoadStatement,
                            maybeGitAdder: Option[GitAdder]) {

  private val diffCalculator = DiffCalculator(maybeBazelRepositoryWithManagedDependencies, resolver, dependenciesRemoteStorage)
  private val diffWriter = DefaultDiffWriter(
    targetRepository,
    maybeManagedDepsRepoPath = maybeBazelRepositoryWithManagedDependencies.map(_.repoPath),
    neverLinkResolver,
    importExternalLoadStatement,
    maybeGitAdder)

  def sync(userAddedDependencies: Set[Dependency], localNodes: Set[DependencyNode]): Unit = {
    val updatedLocalNodes = diffCalculator.calculateDivergentDependencies(localNodes)

    diffWriter.persistResolvedDependencies(userAddedDependencies, updatedLocalNodes, localNodes)
  }
}

case class DiffCalculator(maybeBazelRepositoryWithManagedDependencies: Option[BazelRepository],
                          resolver: MavenDependencyResolver,
                          dependenciesRemoteStorage: DependenciesRemoteStorage) {
  def calculateDivergentDependencies(localNodes: Set[DependencyNode]): Set[BazelDependencyNode] = {
    val managedNodes = maybeBazelRepositoryWithManagedDependencies.map { repoWithManaged =>
      val reader = new BazelDependenciesReader(repoWithManaged.resetAndCheckoutMaster())
      val managedDeps = reader.allDependenciesAsMavenDependencies().toList

      resolver.dependencyClosureOf(managedDeps, withManagedDependencies = managedDeps)
    }.getOrElse(Set.empty)

    calculateDivergentDependencies(localNodes, managedNodes)
  }

  private def calculateDivergentDependencies(localNodes: Set[DependencyNode], managedNodes: Set[DependencyNode]): Set[BazelDependencyNode] = {
    val divergentLocalDependencies = localNodes.forceCompileScopeIfNotProvided diff managedNodes

    decorateNodesWithChecksum(divergentLocalDependencies)(dependenciesRemoteStorage)
  }
}


trait DiffWriter {
  def persistResolvedDependencies(userAddedDependencies: Set[Dependency],
                                  divergentLocalDependencies: Set[BazelDependencyNode],
                                  libraryRulesNodes: Set[DependencyNode],
                                  localDepsToDelete: Set[DependencyNode] = Set()): Unit
}

case class DefaultDiffWriter(targetRepository: BazelRepository,
                             maybeManagedDepsRepoPath: Option[String],
                             neverLinkResolver: NeverLinkResolver,
                             importExternalLoadStatement: ImportExternalLoadStatement,
                             maybeGitAdder: Option[GitAdder]) extends DiffWriter {
  private val log = LoggerFactory.getLogger(getClass)

  def persistResolvedDependencies(userAddedDependencies: Set[Dependency],
                                  divergentLocalDependencies: Set[BazelDependencyNode],
                                  libraryRulesNodes: Set[DependencyNode],
                                  localDepsToDelete: Set[DependencyNode]): Unit = {

    val localCopy = targetRepository.resetAndCheckoutMaster()
    val writer = new BazelDependenciesWriter(localCopy, neverLinkResolver, importExternalLoadStatement)
    //can be removed at phase 2
    val nodesWithPomPackaging = libraryRulesNodes.filter(_.baseDependency.coordinates.packaging.value == "pom").map(_.toBazelNode)

    writer.writeDependencies(
      userAddedDependencies,
      divergentLocalDependencies,
      divergentLocalDependencies ++ nodesWithPomPackaging,
      localDepsToDelete.map(_.baseDependency.coordinates)
    )

    val modifiedFilesToPersist = writer.computeAffectedFilesBy((divergentLocalDependencies ++ nodesWithPomPackaging).map(_.toMavenNode))
    log.info(s"modified ${modifiedFilesToPersist.size} files.")

    //note - the fileSet here sometimes contains also files with no actual changes.. (this is the same messed up file counts printed out here)
    maybeGitAdder.foreach(_.addFiles(modifiedFilesToPersist))

    // note - localDepsToDelete that resulted in deleted files NOT part of modifiedFiles.
    // reason is that they are actually not used in the one case that uses GitBazelRepo (bcos the webapp only syncs core-server-build-tools and never deletes anything there)
    // possible todo - implement Git delete command and fun and unify this split
    val modifiedFilesNotToPersist = writer.computeAffectedFilesBy(localDepsToDelete)
    log.info(s"cleaned deps from ${modifiedFilesNotToPersist.size} files.")
  }
}