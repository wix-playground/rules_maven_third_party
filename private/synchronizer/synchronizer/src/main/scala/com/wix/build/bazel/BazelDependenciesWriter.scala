package com.wix.build.bazel

import com.wix.build.maven._
import com.wix.build.maven.translation.MavenToBazelTranslations.`Maven Coordinates to Bazel rules`

class BazelDependenciesWriter(localWorkspace: BazelLocalWorkspace,
                              neverLinkResolver: NeverLinkResolver = NeverLinkResolver(),
                              importExternalLoadStatement: ImportExternalLoadStatement) {

  private val thirdPartyPaths = localWorkspace.thirdPartyPaths

  val importExternalTargetsFile: ImportExternalTargetsFile = ImportExternalTargetsFile(importExternalLoadStatement, localWorkspace)
  val ruleResolver: RuleResolver = new RuleResolver(localWorkspace.localWorkspaceName, localWorkspace.thirdPartyPaths.thirdPartyImportFilesPathRoot)
  val annotatedDepNodeTransformer: AnnotatedDependencyNodeTransformer = new AnnotatedDependencyNodeTransformer(neverLinkResolver, localWorkspace.thirdPartyPaths.thirdPartyImportFilesPathRoot)

  def writeDependencies(dependencyNodes: BazelDependencyNode*): Set[String] =
    writeDependencies(dependencyNodes.toSet)

  def writeDependencies(dependencyNodes: Set[BazelDependencyNode]): Set[String] = {
    writeThirdPartyFolderContent(dependencyNodes, deleteOld = true)
    writeThirdPartyReposFile(dependencyNodes, findNoLongerUsedGroupIds())
    computeAffectedFilesBy(dependencyNodes.map(_.toMavenNode))
  }

  private def findNoLongerUsedGroupIds(): Set[String] = {
    val importFileGroups = localWorkspace.allThirdPartyImportTargetsGroups()
    val loadedGroups = localWorkspace.allThirdPartyFileLoadedGroups()
    loadedGroups.diff(importFileGroups)
  }

  def writeDependencies(userAddedDependecies: Set[Dependency],
                        dependenciesForThirdPartyReposFile: Set[BazelDependencyNode],
                        dependenciesForThirdPartyFolder: Set[BazelDependencyNode],
                        localDepsToDelete: Set[Coordinates]): Unit = {

    writeThirdPartyFolderContent(dependenciesForThirdPartyFolder, deleteOld = false)
    localDepsToDelete.foreach(overwriteThirdPartyFolderFilesWithDeletedContent)

    val noLongerUsedGroupIds = localDepsToDelete.filter(depToDelete => localWorkspace.thirdPartyImportTargetsFileContent(ImportExternalRule.ruleLocatorFrom(depToDelete)).isEmpty)
    writeThirdPartyReposFile(dependenciesForThirdPartyReposFile, noLongerUsedGroupIds.map(_.groupIdForBazel))
    writeLocalArtifactOverridesFile(userAddedDependecies)
  }

  def computeAffectedFilesBy(dependencyNodes: Set[DependencyNode]): Set[String] = {
    val affectedFiles = dependencyNodes.map(_.baseDependency.coordinates).flatMap(findFilesAccordingToPackagingOf)
    affectedFiles + thirdPartyPaths.thirdPartyReposFilePath
  }

  private def writeLocalArtifactOverridesFile(userAddedDependecies: Set[Dependency]): Unit = {
    val currentContent = localWorkspace.localArtifactOverridesFileContent()
    val updatedContent = LocalArtifactOverridesFile.updateContent(currentContent, userAddedDependecies)
    localWorkspace.overwriteLocalArtifactOverridesFile(updatedContent)
  }

  private def writeThirdPartyReposFile(dependencyNodes: Set[BazelDependencyNode], noLongerUsedGroupIds: Set[String]): Unit = {
    val actual = dependencyNodes.toList.sortBy(_.baseDependency.coordinates.workspaceRuleName)
    val existingThirdPartyReposFile = localWorkspace.thirdPartyReposFileContent()
    val thirdPartyReposBuilder = actual.map(_.baseDependency.coordinates)
      .foldLeft(ThirdPartyReposFile.Builder(existingThirdPartyReposFile))(_.fromCoordinates(_, thirdPartyPaths.thirdPartyImportFilesPathRoot))

    val thirdPartyReposBuilderWithDeletions = noLongerUsedGroupIds.foldLeft(thirdPartyReposBuilder)(_.removeGroupIds(_, thirdPartyPaths.thirdPartyImportFilesPathRoot))
    val content = thirdPartyReposBuilderWithDeletions.content

    val nonEmptyContent = Option(content).filter(_.trim.nonEmpty).fold("  pass")(c => c)
    localWorkspace.overwriteThirdPartyReposFile(nonEmptyContent)
  }

  private def writeThirdPartyFolderContent(dependencyNodes: Set[BazelDependencyNode], deleteOld: Boolean): Unit = {
    if (deleteOld)
      localWorkspace.deleteAllThirdPartyImportTargetsFiles()

    val annotatedDependencyNodes = dependencyNodes.map(annotatedDepNodeTransformer.annotate)

    val targetsToPersist = annotatedDependencyNodes.flatMap(maybeRuleBy)
    val groupedTargets = targetsToPersist.groupBy(_.ruleTargetLocator).values
    groupedTargets.foreach { targetsGroup =>
      val sortedTargets = targetsGroup.toSeq.sortBy(_.rule.name)
      sortedTargets.foreach(overwriteThirdPartyFolderFiles)
    }
  }

  private def maybeRuleBy(dependencyNode: AnnotatedDependencyNode): Option[RuleToPersist] =
    dependencyNode.baseDependency.coordinates.packaging match {
      case Packaging("pom") | Packaging("jar") => Some(createRuleBy(dependencyNode))
      case _ => None
    }

  private def createRuleBy(dependencyNode: AnnotatedDependencyNode): RuleToPersist = {
    val runtimeDependenciesOverrides = localWorkspace.thirdPartyOverrides().runtimeDependenciesOverridesOf(
      OverrideCoordinates(dependencyNode.baseDependency.coordinates.groupId,
        dependencyNode.baseDependency.coordinates.artifactId)
    )

    val compileTimeDependenciesOverrides = localWorkspace.thirdPartyOverrides().compileTimeDependenciesOverridesOf(
      OverrideCoordinates(dependencyNode.baseDependency.coordinates.groupId,
        dependencyNode.baseDependency.coordinates.artifactId)
    )

    val ruleToPersist = ruleResolver.`for`(
      artifact = dependencyNode.baseDependency.coordinates,
      aliases = dependencyNode.baseDependency.aliases,
      tags = dependencyNode.baseDependency.tags,
      runtimeDependencies = dependencyNode.runtimeDependencies,
      compileTimeDependencies = dependencyNode.compileTimeDependencies,
      transitiveClosureDeps = dependencyNode.transitiveClosureDeps,
      exclusions = dependencyNode.baseDependency.exclusions,
      checksum = dependencyNode.checksum,
      srcChecksum = dependencyNode.srcChecksum,
      snapshotSources = dependencyNode.snapshotSources,
      neverlink = dependencyNode.neverlink
    )

    // TODO: try to move this BEFORE the `for` so won't need `withUpdateDeps` in trait
    ruleToPersist.withUpdateDeps(runtimeDependenciesOverrides, compileTimeDependenciesOverrides)
  }

  private def overwriteThirdPartyFolderFiles(ruleToPersist: RuleToPersist): Unit = {
    BazelBuildFile.persistTarget(ruleToPersist, localWorkspace)
    importExternalTargetsFile.persistTarget(ruleToPersist)
  }

  private def overwriteThirdPartyFolderFilesWithDeletedContent(coordsToDelete: Coordinates): Unit = {
    importExternalTargetsFile.deleteTarget(coordsToDelete, localWorkspace)
  }

  private def findFilesAccordingToPackagingOf(artifact: Coordinates): Option[String] = {
    artifact.packaging match {
      case Packaging("jar") => ImportExternalRule.importExternalFilePathBy(artifact, thirdPartyPaths.thirdPartyImportFilesPathRoot)

      case _ => LibraryRule.buildFilePathBy(artifact, thirdPartyPaths.thirdPartyImportFilesPathRoot)
    }
  }
}

case class AnnotatedDependencyNode(baseDependency: Dependency,
                                   runtimeDependencies: Set[BazelDep],
                                   compileTimeDependencies: Set[BazelDep],
                                   transitiveClosureDeps: Set[BazelDep],
                                   checksum: Option[String] = None,
                                   srcChecksum: Option[String] = None,
                                   snapshotSources: Boolean = false,
                                   neverlink: Boolean = false)


class AnnotatedDependencyNodeTransformer(neverLinkResolver: NeverLinkResolver = new NeverLinkResolver(), thirdPartyPath: String) {
  private val resolveNonProto: PartialFunction[Coordinates, BazelDep] = {
    case coordinates if !coordinates.isProtoArtifact => resolveDepBy(coordinates)
  }


  def annotate(dependencyNode: BazelDependencyNode): AnnotatedDependencyNode = {
    AnnotatedDependencyNode(
      baseDependency = dependencyNode.baseDependency,
      runtimeDependencies = dependencyNode.runtimeDependencies.collect(resolveNonProto),
      compileTimeDependencies = dependencyNode.compileTimeDependencies.collect(resolveNonProto),
      transitiveClosureDeps = dependencyNode.transitiveCompileTimeDependencies.collect(resolveNonProto),
      checksum = dependencyNode.checksum,
      srcChecksum = dependencyNode.srcChecksum,
      snapshotSources = dependencyNode.snapshotSources,
      neverlink = neverLinkResolver.isNeverLink(dependencyNode.baseDependency)
    )
  }

  private def resolveDepBy(coordinates: Coordinates): BazelDep = {
    coordinates.packaging match {
      case Packaging("jar") => ImportExternalDep(coordinates, neverLinkResolver.isLinkable(coordinates))
      case _ => LibraryRuleDep(coordinates, thirdPartyPath)
    }
  }
}
