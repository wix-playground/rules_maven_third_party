package com.wix.build.bazel

import com.wix.build.bazel.LibraryRule.{LibraryRuleType, ScalaImportRuleType}
import com.wix.build.maven._
import com.wix.build.translation.MavenToBazelTranslations._

// going to be deprecated when switching to phase 2
// kept for now to support pom artifact migration
case class LibraryRule(name: String,
                       sources: Set[String] = Set.empty,
                       jars: Set[String] = Set.empty,
                       exports: Set[String] = Set.empty,
                       runtimeDeps: Set[String] = Set.empty,
                       compileTimeDeps: Set[String] = Set.empty,
                       exclusions: Set[String] = Set.empty,
                       data: Set[String] = Set.empty,
                       testOnly: Boolean = false,
                       libraryRuleType: LibraryRuleType = ScalaImportRuleType
                      ) extends RuleWithDeps {

  def withRuntimeDeps(runtimeDeps: Set[String]): LibraryRule = this.copy(runtimeDeps = runtimeDeps)

  def serialized: String = {
    s"""${libraryRuleType.name}(
       |    name = "$name",$serializedTestOnly$serializedAttributes
       |)""".stripMargin
  }

  private def serializedTestOnly =
    if (testOnly)
      """
        |    testonly_ = 1,""".stripMargin else ""

  private def serializedAttributes =
    toListEntry("jars", jars) +
      toListEntry("srcs", sources) +
      toListEntry("exports", exports) +
      toListEntry("deps", compileTimeDeps) +
      toListEntry("runtime_deps", runtimeDeps) +
      toListEntry("data", data) +
      toListEntry("excludes", exclusions)


  private def toListEntry(keyName: String, elements: Iterable[String]): String = {
    if (elements.isEmpty) "" else {
      s"""
         |    $keyName = [
         |        ${toStringsList(elements)}
         |    ],""".stripMargin
    }
  }

  private def toStringsList(elements: Iterable[String]) = {
    elements.toList.sorted
      .map(e => s""""$e",""")
      .mkString("\n        ")
  }

  override def updateDeps(runtimeDeps: Set[String], compileTimeDeps: Set[String]): LibraryRule =
    copy(runtimeDeps = runtimeDeps, compileTimeDeps = compileTimeDeps)
}


object LibraryRule {
  val RuleType = "scala_import"

  def pomLibraryRule(artifact: Coordinates,
                     runtimeDependencies: Set[BazelDep],
                     compileTimeDependencies: Set[BazelDep],
                     exclusions: Set[Exclusion],
                     testOnly: Boolean = false): LibraryRule = {
    LibraryRule(
      name = artifact.libraryRuleName,
      jars = Set.empty,
      exports = compileTimeDependencies.map(_.toLabel),
      runtimeDeps = runtimeDependencies.map(_.toLabel),
      exclusions = exclusions.map(_.serialized),
      testOnly = testOnly
    )
  }

  def packageNameBy(coordinates: Coordinates, destination: String): String = s"$destination/${coordinates.groupId.replace('.', '/')}"

  def nonJarLabelBy(coordinates: Coordinates, destination: String): String = {
    s"//${packageNameBy(coordinates, destination)}:${coordinates.libraryRuleName}"
  }

  def buildFilePathBy(coordinates: Coordinates, destination: String): Option[String] = {
    coordinates.packaging match {
      case Packaging("jar") | Packaging("pom") => Some(packageNameBy(coordinates, destination) + "/BUILD.bazel")
      case _ => None
    }
  }

  sealed trait LibraryRuleType {
    def name: String
  }

  case object ScalaLibraryRuleType extends LibraryRuleType {
    val name = "scala_library"
  }

  case object JavaLibraryRuleType extends LibraryRuleType {
    val name = "java_library"
  }

  case object ScalaImportRuleType extends LibraryRuleType {
    val name = "scala_import"
  }
}