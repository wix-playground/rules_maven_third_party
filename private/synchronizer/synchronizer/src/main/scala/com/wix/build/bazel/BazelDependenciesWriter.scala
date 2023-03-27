package com.wix.build.bazel

import com.wix.build.maven._
import com.wix.build.translation.MavenToBazelTranslations.`Maven Coordinates to Bazel rules`

import scala.collection.immutable

class BazelDependenciesWriter(localWorkspace: BazelLocalWorkspace,
                              neverLinkResolver: NeverLinkResolver = NeverLinkResolver(),
                              importExternalLoadStatement: ImportExternalLoadStatement) {

  private val thirdPartyPaths = localWorkspace.thirdPartyPaths

  val importExternalTargetsFile: ImportExternalTargetsFile = ImportExternalTargetsFile(importExternalLoadStatement, localWorkspace)
  val ruleResolver: RuleResolver = new RuleResolver(localWorkspace.thirdPartyPaths.thirdPartyImportFilesPathRoot)
  val annotatedDepNodeTransformer: AnnotatedDependencyNodeTransformer = new AnnotatedDependencyNodeTransformer(neverLinkResolver, localWorkspace.thirdPartyPaths.thirdPartyImportFilesPathRoot)

  def writeDependencies(dependencyNodes: BazelDependencyNode*): Set[String] =
    writeDependencies(dependencyNodes.toSet)

  def writeDependencies(dependencyNodes: Set[BazelDependencyNode]): Set[String] = {
    writeThirdPartyFolderContent(dependencyNodes, deleteOld = true)
    writeThirdPartyReposFile(dependencyNodes, findNoLongerUsedGroupIds())
    computeAffectedFilesBy(dependencyNodes.map(_.toMavenNode))
  }

  def writeFromSratchDependencies(dependencyNodes: Set[BazelDependencyNode]): Set[String] = {
    localWorkspace.deleteAllThirdPartyImportTargetsFiles()
    localWorkspace.overwriteThirdPartyReposFile("def dependencies():")
    writeThirdPartyFolderContent(dependencyNodes, deleteOld = false, addRemmaping = true)
    writeReceipt(dependencyNodes)
    writeThirdPartyReposFile(dependencyNodes, Set())
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

    val noLongerUsedGroupIds = localDepsToDelete
      .filter(depToDelete => localWorkspace.thirdPartyImportTargetsFileContent(ImportExternalRule.ruleLocatorFrom(depToDelete)).isEmpty)
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

  private def writeReceipt(dependencyNodes: Set[BazelDependencyNode]): Unit = {
    localWorkspace.writeReceipt(
      dependencyNodes.map { node =>
        import node.baseDependency._
        val isJar = node.baseDependency.coordinates.packaging == Packaging("jar")

        val coords = coordinates.serialized
        val label = s"@${coordinates.workspaceRuleName}"
        val canonicalLabel = s"@${coordinates.workspaceRuleNameVersioned}"

        s"$coords $label ${if (isJar) canonicalLabel else label}"
      }.mkString("\n")
    )
  }

  private def writeThirdPartyFolderContent(dependencyNodes: Set[BazelDependencyNode],
                                           deleteOld: Boolean,
                                           addRemmaping: Boolean = false): Unit = {
    if (deleteOld)
      localWorkspace.deleteAllThirdPartyImportTargetsFiles()

    val annotatedDependencyNodes = dependencyNodes.map(annotatedDepNodeTransformer.annotate)

    val targetsToPersist = if (addRemmaping) {
      val userLabelsToVersions = dependencyNodes.toList
        .map { dep =>
          val coordinates = dep.baseDependency.coordinates
          ("@" + coordinates.workspaceRuleName) -> ("@" + coordinates.workspaceRuleNameVersioned)
        }.toMap

      annotatedDependencyNodes.flatMap(node => maybeRuleWithRemapping(node, userLabelsToVersions))

    } else
      annotatedDependencyNodes.flatMap(maybeRuleBy)

    val groupedTargets = targetsToPersist.groupBy(_.ruleTargetLocator).values
    groupedTargets.foreach { targetsGroup =>
      val sortedTargets = targetsGroup.toSeq.sortBy(_.rule.name)
      sortedTargets.foreach(overwriteThirdPartyFolderFiles)
    }
  }

  private def maybeRuleBy(dependencyNode: AnnotatedDependencyNode): Option[RuleToPersist] =
    dependencyNode.baseDependency.coordinates.packaging match {
      case Packaging("pom") | Packaging("jar") => Some(createRuleBy(dependencyNode, Map.empty))
      case _ => None
    }

  private def maybeRuleWithRemapping(dependencyNode: AnnotatedDependencyNode,
                                     overriddenLabels: Map[String, String]): Option[RuleToPersist] = {
    dependencyNode.baseDependency.coordinates.packaging match {
      case Packaging("pom") | Packaging("jar") => Some(createRuleBy(dependencyNode, overriddenLabels))
      case _ => None
    }
  }

  private def collectMappings(node: AnnotatedDependencyNode, overriddenLabels: Map[String, String]): Map[String, String] = {
    val deps = node.compileTimeDependencies.map(_.toLabel) ++
      node.runtimeDependencies.map(_.toLabel) + s"@${node.baseDependency.coordinates.workspaceRuleName}"

    deps.collect { case dep if overriddenLabels.contains(dep) =>
        dep -> overriddenLabels(dep)
    }.toMap
  }

  private def createRuleBy(dependencyNode: AnnotatedDependencyNode, overriddenLabels: Map[String, String]): RuleToPersist = {
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
      neverlink = dependencyNode.neverlink,
      remapping = if (overriddenLabels.nonEmpty) collectMappings(dependencyNode, overriddenLabels) else Map.empty
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
