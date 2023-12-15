package com.wix.build

import com.wix.build.bazel.ThirdPartyReposFile.{serializedImportExternalTargetsFileMethodCall, serializedLoadImportExternalTargetsFile}
import com.wix.build.bazel._
import com.wix.build.maven.Coordinates._
import com.wix.build.maven._
import com.wix.build.translation.MavenToBazelTranslations._
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._

class BazelWorkspaceDriver(bazelRepo: BazelLocalWorkspace) {

  def writeDependenciesAccordingTo(dependencies: Set[MavenJarInBazel]): Unit = {
    val allJarsImports = dependencies.map(_.artifact) ++ dependencies.flatMap(_.runtimeDependencies)
    val newThirdPartyRepos = allJarsImports.foldLeft(bazelRepo.thirdPartyReposFileContent())(addImportFileLoadStatementsToThirdPartyReposFile)
    bazelRepo.overwriteThirdPartyReposFile(newThirdPartyRepos)
    dependencies.foreach(updateImportExternalTargetsFile)
  }

  def versionOfImportedJar(coordinates: Coordinates): Option[String] = {
    bazelExternalDependencyFor(coordinates).importExternalRule.map(r => deserialize(r.artifact).version)
  }

  def transitiveCompileTimeDepOf(coordinates: Coordinates): Set[String] = {
    bazelExternalDependencyFor(coordinates).importExternalRule.fold(Set[String]())(_.compileTimeDeps)
  }

  def bazelExternalDependencyFor(coordinates: Coordinates): BazelExternalDependency = {
    val maybeImportExternalRule = findImportExternalRuleBy(coordinates)
    val maybeLibraryRule = findLibraryRuleBy(coordinates)
    BazelExternalDependency(maybeImportExternalRule, maybeLibraryRule)
  }

  def findImportExternalRuleBy(coordinates: Coordinates): Option[ImportExternalRule] = {
    val groupId = coordinates.groupIdForBazel
    val targetName = coordinates.workspaceRuleName
    val maybeImportFile = bazelRepo.thirdPartyImportTargetsFileContent(groupId)
    maybeImportFile.flatMap(ImportExternalTargetsFileReader(_).ruleByName(targetName))
  }

  def findLibraryRuleBy(coordinates: Coordinates): Option[LibraryRule] = {
    val packageName = LibraryRule.packageNameBy(coordinates, bazelRepo.thirdPartyPaths.thirdPartyImportFilesPathRoot)
    val targetName = coordinates.libraryRuleName
    val maybeBuildFile = bazelRepo.buildFileContent(packageName)
    maybeBuildFile.flatMap(BazelBuildFile(_).ruleByName(targetName))
  }

  private def updateImportExternalTargetsFile(mavenJarInBazel: MavenJarInBazel): Unit = {
    import mavenJarInBazel._
    val rule = ImportExternalRule.of(
      artifact,
      runtimeDependencies = runtimeDependencies.map(ImportExternalDep(_)),
      compileTimeDependencies = compileTimeDependencies.map(ImportExternalDep(_)),
      exclusions = exclusions
    )
    val artifactGroup = artifact.groupIdForBazel

    val importExternalTargetsFileContent = bazelRepo.thirdPartyImportTargetsFileContent(artifactGroup).getOrElse("")
    val newContent =
      s"""$importExternalTargetsFileContent
         |
         |${rule.serialized}
       """.stripMargin

    bazelRepo.overwriteThirdPartyImportTargetsFile(artifactGroup, newContent)
  }

  private def addImportFileLoadStatementsToThirdPartyReposFile(currentSkylarkFile: String, mavenJar: Coordinates) = {
    s"""${serializedLoadImportExternalTargetsFile(mavenJar, bazelRepo.thirdPartyPaths.destinationPackage)}
       |
       |$currentSkylarkFile
       |
       |${serializedImportExternalTargetsFileMethodCall(mavenJar)}
       |""".stripMargin
  }
}

object BazelWorkspaceDriver {
  val localWorkspaceName = "some_local_workspace_name"

  implicit class BazelWorkspaceDriverExtensions(w: BazelLocalWorkspace) {
    def hasDependencies(dependencyNodes: BazelDependencyNode*) = {
      new BazelDependenciesWriter(
        localWorkspace = w,
        importExternalLoadStatement = ImportExternalLoadStatement(
          importExternalRulePath = "@some_workspace//:import_external.bzl",
          importExternalMacroName = "some_import_external",
        ),
      ).writeDependencies(dependencyNodes.toSet)
    }
  }

  def includeLibraryRuleTarget(artifact: Coordinates, expectedlibraryRule: LibraryRule): Matcher[BazelWorkspaceDriver] = { driver: BazelWorkspaceDriver =>
    driver.bazelExternalDependencyFor(artifact).equals(BazelExternalDependency(importExternalRule = None,
      libraryRule = Some(expectedlibraryRule)))
  }

  def resolveDepBy(coordinates: Coordinates, thirdPartyPath: String): BazelDep = {
    coordinates.packaging match {
      case Packaging("jar") => ImportExternalDep(coordinates)
      case _ => LibraryRuleDep(coordinates, thirdPartyPath)
    }
  }

  private def importExternalRuleWith(artifact: Coordinates,
                                     runtimeDependencies: Set[Coordinates],
                                     compileTimeDependencies: Set[Coordinates],
                                     exclusions: Set[Exclusion],
                                     testOnly: Boolean,
                                     checksum: Option[String],
                                     coordinatesToDep: (Coordinates, String) => BazelDep,
                                     thirdPartyPath: String,
                                     srcChecksum: Option[String],
                                     neverlink: Boolean,
                                     snapshotSources: Boolean) = {
    ImportExternalRule.of(
      artifact,
      runtimeDependencies = runtimeDependencies.map(coordinatesToDep(_, thirdPartyPath)),
      compileTimeDependencies = compileTimeDependencies.map(coordinatesToDep(_, thirdPartyPath)),
      exclusions = exclusions,
      testOnly = testOnly,
      checksum = checksum,
      srcChecksum = srcChecksum,
      snapshotSources = snapshotSources,
      neverlink = neverlink
    )
  }

  def includeImportExternalTargetWith(artifact: Coordinates,
                                      runtimeDependencies: Set[Coordinates] = Set.empty,
                                      compileTimeDependenciesIgnoringVersion: Set[Coordinates] = Set.empty,
                                      exclusions: Set[Exclusion] = Set.empty,
                                      testOnly: Boolean = false,
                                      checksum: Option[String] = None,
                                      coordinatesToDep: (Coordinates, String) => BazelDep = resolveDepBy,
                                      thirdPartyPath: String,
                                      srcChecksum: Option[String] = None,
                                      neverlink: Boolean = false,
                                      snapshotSources: Boolean = false): Matcher[BazelWorkspaceDriver] =

    be_===(BazelExternalDependency(
      importExternalRule = Some(importExternalRuleWith(
        artifact = artifact,
        runtimeDependencies = runtimeDependencies,
        compileTimeDependencies = compileTimeDependenciesIgnoringVersion,
        exclusions = exclusions,
        testOnly = testOnly,
        checksum = checksum,
        coordinatesToDep,
        srcChecksum = srcChecksum,
        snapshotSources = snapshotSources,
        neverlink = neverlink,
        thirdPartyPath = thirdPartyPath)))) ^^ {
      (_: BazelWorkspaceDriver).bazelExternalDependencyFor(artifact) aka s"bazel workspace does not include external deps target for $artifact"
    }

  def includeLibraryTargetWith(artifact: Coordinates,
                               testOnly: Boolean = false): Matcher[BazelWorkspaceDriver] =

    be_===(BazelExternalDependency(
      importExternalRule = None,
      libraryRule = Some(LibraryRule(
        name = artifact.libraryRuleName,
        testOnly = testOnly,
      ))
    )) ^^ {
      (_: BazelWorkspaceDriver).bazelExternalDependencyFor(artifact) aka s"bazel workspace does not include external deps target for $artifact"
    }

  def notIncludeImportExternalRulesInWorkspace(coordinatesSet: Coordinates*): Matcher[BazelWorkspaceDriver] = notIncludeImportExternalRulesInWorkspace(coordinatesSet.toSet)

  def notIncludeImportExternalRulesInWorkspace(coordinatesSet: Set[Coordinates]): Matcher[BazelWorkspaceDriver] = coordinatesSet.map(notIncludeJarInWorkspace).reduce(_.and(_))

  private def notIncludeJarInWorkspace(coordinates: Coordinates): Matcher[BazelWorkspaceDriver] = { driver: BazelWorkspaceDriver =>
    (driver.bazelExternalDependencyFor(coordinates).importExternalRule.isEmpty, s"unexpected $coordinates were found in project")
  }

  def of[T](x: T): T = x
}

case class MavenJarInBazel(artifact: Coordinates, runtimeDependencies: Set[Coordinates], compileTimeDependencies: Set[Coordinates], exclusions: Set[Exclusion])

case class BazelExternalDependency(importExternalRule: Option[ImportExternalRule], libraryRule: Option[LibraryRule] = None)
