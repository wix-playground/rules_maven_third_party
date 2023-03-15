package com.wix.build.bazel

import com.wix.build.bazel.ImportExternalRule.RuleType
import com.wix.build.maven._
import com.wix.build.translation.MavenToBazelTranslations._

case class ImportExternalRule(name: String,
                              artifact: String,
                              aliases: Set[String] = Set.empty,
                              tags: Set[String] = Set.empty,
                              exports: Set[String] = Set.empty,
                              runtimeDeps: Set[String] = Set.empty,
                              compileTimeDeps: Set[String] = Set.empty,
                              transitiveClosureDeps: Set[String] = Set.empty,
                              exclusions: Set[String] = Set.empty,
                              testOnly: Boolean = false,
                              checksum: Option[String] = None,
                              srcChecksum: Option[String] = None,
                              snapshotSources: Boolean = false,
                              neverlink: Boolean = false,
                              remapping: Map[String, String] = Map()) extends RuleWithDeps {

  def serialized: String = {
    s"""    $RuleType(
       |        name = "$name",
       |        $serializedArtifact$serializedTestOnly$serializedChecksum$serializedSrcChecksum$serializedSnapshotSources$serializedAttributes$serializedNeverlink
       |    )""".stripMargin
  }

  private def serializedArtifact =
    s"""|artifact = "$artifact",""".stripMargin

  private def serializedTestOnly =
    if (testOnly)
      """
        |        testonly_ = 1,""".stripMargin else ""

  private def serializedChecksum: String = {
    val noChecksumDefault = if (!artifact.endsWith("-SNAPSHOT")) "        # fixme: missing jar" else ""
    checksum.fold(noChecksumDefault) { sha256 =>
      s"""
         |        artifact_sha256 = "$sha256",""".stripMargin
    }
  }

  private def serializedSrcChecksum =
    srcChecksum.fold("")(sha256 =>
      s"""
         |        srcjar_sha256 = "$sha256",""".stripMargin)

  private def serializedSnapshotSources =
    if (snapshotSources)
      """
        |        snapshot_sources = 1,""".stripMargin else ""


  private def serializedAttributes =
    toListEntry("exports", exports) +
      toListEntry("deps", compileTimeDeps) +
      toListEntry("runtime_deps", runtimeDeps) +
      toListEntry("transitive_closure_deps", transitiveClosureDeps) +
      toListEntry("excludes", exclusions) +
      toListEntry("aliases", aliases) +
      toListEntry("tags", tags) +
      toDictEntry("remapping", remapping)

  private def serializedNeverlink =
    if (neverlink)
      """
        |        neverlink = 1,
        |        generated_linkable_rule_name = "linkable",""".stripMargin else ""

  private def toListEntry(keyName: String, elements: Iterable[String]): String = {
    if (elements.isEmpty) "" else {
      s"""
         |        $keyName = [
         |            ${toStringsList(elements)}
         |        ],""".stripMargin
    }
  }

  private def toDictEntry(keyName: String, map: Map[String, String]): String = {
    if (map.isEmpty)
      ""
    else
      s"""
         |        $keyName = {
         |            ${toPairsString(map)}
         |        },""".stripMargin
  }

  private def toStringsList(elements: Iterable[String]): String = {
    elements.toList.sorted
      .map(e => s""""$e",""")
      .mkString("\n            ")
  }

  private def toPairsString(map: Map[String, String]): String = {
    map.map { case (key, value) =>
      s""""$key": "$value","""
    }.mkString("\n            ")
  }

  override def updateDeps(runtimeDeps: Set[String], compileTimeDeps: Set[String]): ImportExternalRule =
    copy(runtimeDeps = runtimeDeps, compileTimeDeps = compileTimeDeps)

  def withRuntimeDeps(runtimeDeps: Set[String]): ImportExternalRule = this.copy(runtimeDeps = runtimeDeps)
}

object ImportExternalRule {
  val RuleType = "import_external"

  def of(artifact: Coordinates,
         aliases: Set[String] = Set.empty,
         tags: Set[String] = Set.empty,
         runtimeDependencies: Set[BazelDep] = Set.empty,
         compileTimeDependencies: Set[BazelDep] = Set.empty,
         transitiveClosureDeps: Set[BazelDep] = Set.empty,
         exclusions: Set[Exclusion] = Set.empty,
         checksum: Option[String] = None,
         srcChecksum: Option[String] = None,
         snapshotSources: Boolean = false,
         neverlink: Boolean = false,
         testOnly: Boolean = false,
         remapping: Map[String, String] = Map.empty): ImportExternalRule = {
    ImportExternalRule(
      name = artifact.workspaceRuleName,
      aliases = aliases,
      tags = tags,
      artifact = artifact.serialized,
      compileTimeDeps = compileTimeDependencies.map(_.toLabel),
      runtimeDeps = runtimeDependencies.map(_.toLabel),
      transitiveClosureDeps = transitiveClosureDeps.map(_.toLabel),
      exclusions = exclusions.map(_.serialized),
      testOnly = testOnly,
      checksum = checksum,
      srcChecksum = srcChecksum,
      snapshotSources = snapshotSources,
      neverlink = neverlink,
      remapping = remapping
    )
  }

  def jarLabelBy(coordinates: Coordinates, linkableSuffixNeeded: Boolean = false): String = {
    val suffix = if (linkableSuffixNeeded)
      "//:linkable"
    else
      ""
    s"@${coordinates.workspaceRuleName}$suffix"
  }

  def jarLabelWithVersion(coordinates: Coordinates, linkableSuffixNeeded: Boolean = false): String = {
    val suffix = if (linkableSuffixNeeded)
      "//:linkable"
    else
      ""
    s"@${coordinates.workspaceRuleNameVersioned}$suffix"
  }

  def linkableLabelBy(coordinates: Coordinates): String = s"@${coordinates.workspaceRuleName}//:linkable"

  def importExternalFilePathBy(coordinates: Coordinates, destination: String): Option[String] = {
    coordinates.packaging match {
      case Packaging("jar") =>
        Some(s"$destination/${ruleLocatorFrom(coordinates)}.bzl")
      case _ => None
    }
  }

  def ruleLocatorFrom(coordinates: Coordinates): String = coordinates.groupIdForBazel
}

