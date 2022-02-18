package com.wix.build.bazel

import com.wix.build.maven.{Coordinates, _}

object LocalArtifactOverridesFile {

  private val localOverrideDepsRegex = """LOCAL_OVERRIDE_DEPS\s*=\s*\[\n?([\W\w]*)\]\n?""".r
  private val artifactIdRegex = """\s*artifact\s*=\s*"?([^",]+)"?\s*,""".r

  def updateContent(content: String, userAddedDependecies: Set[Dependency]): String = {
    val updatedManagedDeps = updateManagedDeps(content, userAddedDependecies)
    localOverrideDepsRegex.replaceFirstIn(content, updatedManagedDeps)
  }

  private def updateManagedDeps(content: String, userAddedDependecies: Set[Dependency]): String = {
    val currentDeps = localOverrideDepsRegex
      .findFirstMatchIn(content)
      .map(_.group(1))
      .getOrElse("")
      .split("\n")
      .map(_.replaceFirst(",$", "").trim)
      .filter(_.nonEmpty)
      .map(str => s"    $str,")

    val updatedDeps = addNewDeps(currentDeps, userAddedDependecies)

    s"""LOCAL_OVERRIDE_DEPS = [
       |${updatedDeps.mkString("\n")}
       |]
       |""".stripMargin
  }

  def addNewDeps(currentDeps: Seq[String], userAddedDependecies: Set[Dependency]): Seq[String] = {
    val filteredBazelDeps = filterDeps(currentDeps, userAddedDependecies)
    val newMavenDeps = filteredBazelDeps.map(toMavenArtifact)
    currentDeps ++ newMavenDeps
  }

  private def toMavenArtifact(dependency: Dependency): String = {
    val Coordinates(groupId, artifactId, version, _, _) = dependency.coordinates

    val artifact = Seq(
      s"""group = "$groupId"""",
      s"""artifact = "$artifactId"""",
      s"""version = "$version"""",
      maybeExclusions(dependency.exclusions),
      maybeTags(dependency.tags),
      maybeAliases(dependency.aliases)
    ).filter(_.nonEmpty).mkString(", ")

    s"""    maven.artifact($artifact),"""
  }

  private def maybeTags(tags: Set[String]): String = {
    checkIfNotEmpty(tags, s"tags = [${tags.mkString(", ")}]")
  }

  private def maybeAliases(aliases: Set[String]): String = {
    checkIfNotEmpty(aliases, s"aliases = [${aliases.mkString(", ")}]")
  }

  private def maybeExclusions(exclusions: Set[Exclusion]): String = {
    checkIfNotEmpty(
      exclusions,
      s"exclusions = [${exclusions.map(e => s"""maven.exclusion(artifact = "${e.artifactId}", group = "${e.groupId}")""").mkString(", ")}]"
    )
  }

  private def checkIfNotEmpty[T](items: Set[T], formatted: String): String = {
    if (items.nonEmpty) formatted else ""
  }

  private def filterDeps(currentDeps: Seq[String],
                         userAddedDependecies: Set[Dependency]): Set[Dependency] = {

    val currentArtifactIds = currentDeps.flatMap { dep =>
      artifactIdRegex.findFirstMatchIn(dep).map(_.group(1))
    }

    userAddedDependecies.filter { node =>
      !currentArtifactIds.contains(node.coordinates.artifactId)
    }
  }
}
