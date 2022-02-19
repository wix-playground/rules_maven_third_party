package com.wix.build.bazel

import com.wix.build.maven._
import com.wix.build.translation.MavenToBazelTranslations._

//noinspection RedundantDefaultArgument
class BazelDependenciesReader(localWorkspace: BazelLocalWorkspace) {
  def allDependenciesAsMavenDependencyNodes(externalDeps: Set[Dependency] = Set()): Set[DependencyNode] = {
    val pomAggregatesCoordinates = ThirdPartyReposFile.Parser(localWorkspace.thirdPartyReposFileContent()).allMavenCoordinates

    val importExternalTargetsFileParser = AllImportExternalFilesDependencyNodesReader(
      filesContent = localWorkspace.allThirdPartyImportTargetsFilesContent(),
      pomAggregatesCoordinates,
      externalDeps,
      localWorkspace.localWorkspaceName,
      localWorkspace.thirdPartyPaths.thirdPartyImportFilesPathRoot
    )

    allMavenDependencyNodes(importExternalTargetsFileParser)
  }


  private def allMavenDependencyNodes(importExternalTargetsFileParser: AllImportExternalFilesDependencyNodesReader) = {
    val nodesOrErrors = importExternalTargetsFileParser.allMavenDependencyNodes()
    nodesOrErrors match {
      case Left(nodes) => nodes
      case Right(errorMessages) =>
        throw new RuntimeException(s"${errorMessages.mkString("\n")}\ncannot finish compiling dep closure. please consult with support.")
    }
  }

  def allDependenciesAsMavenDependencies(): Set[Dependency] = {
    val thirdPartyReposParser = ThirdPartyReposFile.Parser(localWorkspace.thirdPartyReposFileContent())

    val importExternalTargetsFileParser = AllImportExternalFilesCoordinatesReader(localWorkspace.allThirdPartyImportTargetsFilesContent())

    val coordinates = importExternalTargetsFileParser.allMavenCoordinates ++ thirdPartyReposParser.allMavenCoordinates.map(ValidatedCoordinates(_, None, None, Set.empty))
    coordinates.map(toDependency)
  }

  private def toDependency(validatedCoordinates: ValidatedCoordinates) = Dependency(validatedCoordinates.coordinates, MavenScope.Compile, isNeverLink = false, exclusions = exclusionsOf(validatedCoordinates.coordinates))

  private def exclusionsOf(coordinates: Coordinates): Set[Exclusion] =
    buildFileRuleExclusionsOf(coordinates) ++ externalImportRuleExclusionsOf(coordinates)

  private def buildFileRuleExclusionsOf(coordinates: Coordinates) = {
    maybeBuildFileContentBy(coordinates)
      .flatMap(findMatchingRule(coordinates))
      .map(_.exclusions)
      .getOrElse(Set.empty)
      .map(Exclusion.apply)
  }

  private def externalImportRuleExclusionsOf(coordinates: Coordinates) = {
    val thirdPartyImportTargetsFileContent = localWorkspace.thirdPartyImportTargetsFileContent(coordinates.groupIdForBazel)
    val importExternalRule = thirdPartyImportTargetsFileContent.flatMap(ImportExternalTargetsFileReader(_).ruleByName(coordinates.workspaceRuleName))
    val importExternalExclusions = importExternalRule.map(_.exclusions).getOrElse(Set.empty)
    importExternalExclusions.map(Exclusion.apply)
  }

  private def maybeBuildFileContentBy(coordinates: Coordinates) = {
    localWorkspace.buildFileContent(LibraryRule.packageNameBy(coordinates, localWorkspace.thirdPartyPaths.thirdPartyImportFilesPathRoot))
  }

  private def findMatchingRule(coordinates: Coordinates)(buildFileContent: String) = {
    BazelBuildFile(buildFileContent).ruleByName(coordinates.libraryRuleName)
  }


}
