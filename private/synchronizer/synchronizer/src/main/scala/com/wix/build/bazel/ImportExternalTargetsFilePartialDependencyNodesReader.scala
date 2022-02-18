package com.wix.build.bazel

import com.wix.build.bazel.EitherSupport.partitionEithers
import com.wix.build.bazel.ImportExternalTargetsFileReader._
import com.wix.build.maven._
import com.wix.build.maven.translation.MavenToBazelTranslations._

case class ImportExternalTargetsFilePartialDependencyNodesReader(content: String, localWorkspaceName: String = "", thirdPartyDestination: String) {
  def allBazelDependencyNodes(): Set[PartialDependencyNode] = {
    splitToStringsWithJarImportsInside(content).flatMap(parse).toSet
  }

  private def parse(importExternalTarget: String) = {
    for {
      name <- parseImportExternalName(importExternalTarget)
      coordinates <- parseCoordinates(importExternalTarget)
      exclusions = extractListByAttribute(ExclusionsFilter, importExternalTarget).map(Exclusion.apply)
      compileDeps = extractListByAttribute(CompileTimeDepsFilter, importExternalTarget)
      runtimeDeps = extractListByAttribute(RunTimeDepsFilter, importExternalTarget)
      isNeverLink = ImportExternalTargetsFileReader.extractNeverlink(importExternalTarget)
    } yield PartialDependencyNode(name, Dependency(coordinates.coordinates, MavenScope.Compile, isNeverLink, exclusions),
      compileDeps.flatMap(d => parseTargetDependency(d, MavenScope.Compile)) ++
        runtimeDeps.flatMap(d => parseTargetDependency(d, MavenScope.Runtime))
    )
  }

  def parseTargetDependency(dep: String, scope: MavenScope): Option[PartialDependency] = {
    dep match {
      case PomAggregateDependencyLabel(ruleName) => Some(PartialPomAggregateDependency(ruleName, scope))
      case SourceDependencyLabel() => None
      case _ => Some(PartialJarDependency(parseImportExternalDep(dep).getOrElse(dep), scope))
    }
  }

  object PomAggregateDependencyLabel {
    def unapply(label: String): Option[String] = parseAggregatePomDep(label).map(_.replace("/", "_").replace(":", "_"))

    private def parseAggregatePomDep(text: String) = {
      AggregatePomDepFilter.findFirstMatchIn(text).map(_.group("ruleName"))
    }

    private val AggregatePomDepFilter = ("""@.*//""" + thirdPartyDestination + """/(.*)""").r("ruleName")
  }

  object SourceDependencyLabel {
    def unapply(label: String): Boolean = {
      label match {
        case SourceDependencyFilter(_, workspaceName, target) => localWorkspaceName.equals(workspaceName) ||
          (!target.equals("jar") && !target.equals(workspaceName))
        case _ => false
      }
    }

    private val SourceDependencyFilter = """(@([^/]*))?//(.*)""".r
  }

}

case class AllImportExternalFilesDependencyNodesReader(filesContent: Set[String],
                                                       pomAggregatesCoordinates: Set[Coordinates],
                                                       externalDeps: Set[Dependency] = Set(),
                                                       localWorkspaceName: String = "", thirdPartyDestination: String) {
  type RuleName = String
  type BaseDependencies = Map[RuleName, Dependency]

  def allMavenDependencyNodes(): Either[Set[DependencyNode], Set[String]] = {
    val bazelDependencyNodes = filesContent.flatMap(c => ImportExternalTargetsFilePartialDependencyNodesReader(c, localWorkspaceName, thirdPartyDestination).allBazelDependencyNodes())
    val baseDependencies: BaseDependencies = bazelDependencyNodes.map(d => d.ruleName -> d.baseDependency).toMap
    val dependencyNodesOrErrors = bazelDependencyNodes.map(d => mavenDependencyNodeFrom(d, baseDependencies))

    transformEithers(dependencyNodesOrErrors)
  }

  private def mavenDependencyNodeFrom(partialNode: PartialDependencyNode, baseDependencies: BaseDependencies): Either[DependencyNode, Set[String]] = {
    val dependenciesOrErrors = partialNode.targetDependencies.map(t => transitiveDepFrom(t, baseDependencies, partialNode.baseDependency.coordinates))

    val (maybeDependencies, rights) = partitionEithers[Option[Dependency], String](dependenciesOrErrors)
    if (rights.nonEmpty)
      Right(rights)
    else
      Left(DependencyNode(
        partialNode.baseDependency,
        maybeDependencies.flatten))
  }

  private def transitiveDepFrom(partialDep: PartialDependency, baseDependencies: BaseDependencies, dependantArtifact: Coordinates): Either[Option[Dependency], String] = {

    def lookupDep: Option[Dependency] = {
      partialDep match {
        //TODO - is this a bug?
        case _: PartialPomAggregateDependency => pomAggregatesCoordinates.find(_.workspaceRuleName == partialDep.ruleName).map(Dependency(_, partialDep.scope))
        case _ => baseDependencies.get(partialDep.ruleName)
          .orElse(externalDeps.find(_.coordinates.workspaceRuleName == partialDep.ruleName))
      }
    }

    val maybeDependency = lookupDep.map(_.copy(scope = partialDep.scope))
    if (maybeDependency.isEmpty) {
      val dependentArtifactLocation = s"$thirdPartyDestination/${dependantArtifact.groupIdForBazel}.bzl"
      val dependentArtifactName = dependantArtifact.shortSerializedForm()
      Right(
        s"""
           |This means that "$dependentArtifactName" declares that it has a dependency called
           |"$partialDep", but that "$partialDep" is not declared in $dependentArtifactLocation file.
           |You can go to $dependentArtifactLocation file to see the dependency declaration of "$partialDep". Then:
           |1. Check if $dependentArtifactName is used and delete if it's not needed.
           |2. Or, delete the "$dependentArtifactName" section and run
           |"bazel run @bazel_tooling//define_maven_deps -- ${dependantArtifact.serialized}"
           |the tool will add this with the full correct closure for you.
           |""".stripMargin)
    } else
      Left(maybeDependency)
  }

  private def transformEithers(dependencyNodesOrErrors: Set[Either[DependencyNode, Set[String]]]) = {
    val (lefts, rights) = partitionEithers(dependencyNodesOrErrors)
    if (rights.nonEmpty)
      Right(rights.flatten)
    else
      Left(lefts)
  }
}

case class PartialDependencyNode(ruleName: String, baseDependency: Dependency, targetDependencies: Set[PartialDependency])

trait PartialDependency {
  val ruleName: String
  val scope: MavenScope

  override def toString: String = ruleName
}

case class PartialJarDependency(ruleName: String, scope: MavenScope) extends PartialDependency

case class PartialPomAggregateDependency(ruleName: String, scope: MavenScope) extends PartialDependency

object EitherSupport {
  def partitionEithers[A, B](eithers: Set[Either[A, B]]): (Set[A], Set[B]) = {
    def lefts = eithers collect { case Left(x) => x }

    val rights = eithers collect { case Right(x) => x }
    (lefts, rights)
  }
}