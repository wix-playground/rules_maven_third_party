package com.wix.build.bazel

import com.wix.build.maven.{Coordinates, Packaging}
import com.wix.build.translation.MavenToBazelTranslations._

import scala.util.matching.Regex

object ThirdPartyReposFile {

  case class Builder(content: String = "") {
    def fromCoordinates(coordinates: Coordinates, destinationPackage: DestinationPackage): Builder = {
      coordinates.packaging match {
        case Packaging("jar") => withLoadStatementsFor(coordinates, destinationPackage)
        case Packaging("war") => unchangedBuilder
        case _ => withMavenArtifact(coordinates)
      }
    }

    def removeGroupIds(groupIdForBazel: String, destinationPackage: DestinationPackage): Builder = {
      import NewLinesParser.removeMatched

      val contentWithoutLoadStatement = regexOfLoadRuleWithNameMatching(groupIdForBazel, destinationPackage)
        .findFirstMatchIn(content) match {
        case Some(m) => removeMatched(content, m)
        case None => content
      }

      val contentWithoutMethodCall = regexOfImportExternalTargetsFileMethodCall(groupIdForBazel)
        .findFirstMatchIn(contentWithoutLoadStatement) match {
        case Some(m) => removeMatched(contentWithoutLoadStatement, m)
        case None => contentWithoutLoadStatement
      }

      Builder(contentWithoutMethodCall)
    }

    def withLoadStatementsFor(coordinates: Coordinates, destinationPackage: DestinationPackage): Builder = {
      val updatedContent = regexOfLoadRuleWithNameMatching(coordinates.groupIdForBazel, destinationPackage)
        .findFirstMatchIn(content) match {
        case None => appendLoadStatements(content, coordinates, destinationPackage)
        case _ => content
      }
      Builder(updatedContent)
    }

    private def appendLoadStatements(thirdPartyRepos: String, coordinates: Coordinates, destinationPackage: DestinationPackage): String = {
      s"""${serializedLoadImportExternalTargetsFile(coordinates, destinationPackage)}
         |
         |${thirdPartyRepos.stripLineEnd}
         |
         |${serializedImportExternalTargetsFileMethodCall(coordinates)}
         |""".stripMargin
    }

    private def unchangedBuilder = {
      Builder(content)
    }

    def withMavenArtifact(coordinates: Coordinates): Builder =
      Builder(newThirdPartyReposWithMavenArchive(coordinates))

    private def newThirdPartyReposWithMavenArchive(coordinates: Coordinates) = {
      regexOfWorkspaceRuleWithNameMatching(coordinates.workspaceRuleName)
        .findFirstMatchIn(content) match {
        case Some(matched) => updateMavenArtifact(content, coordinates, matched)
        case None => appendMavenArtifact(content, coordinates)
      }
    }

    private def updateMavenArtifact(thirdPartyRepos: String, coordinates: Coordinates, matched: Regex.Match): String = {
      val newMavenJarRule = WorkspaceRule.of(coordinates).serialized
      thirdPartyRepos.take(matched.start - "    ".length) + newMavenJarRule + thirdPartyRepos.drop(matched.end)
    }

    private def appendMavenArtifact(thirdPartyRepos: String, coordinates: Coordinates): String =
      s"""$thirdPartyRepos
         |
         |${WorkspaceRule.of(coordinates).serialized}
         |""".stripMargin

  }

  def serializedLoadImportExternalTargetsFile(fromCoordinates: Coordinates, destinationPackage: DestinationPackage): String = {
    val groupId = fromCoordinates.groupIdForBazel
    s"""load("${destinationPackage.bazelPackage}/${groupId}.bzl", ${groupId}_deps = "dependencies")"""
  }

  def serializedImportExternalTargetsFileMethodCall(fromCoordinates: Coordinates): String = {
    val groupId = fromCoordinates.groupIdForBazel
    s"    ${groupId}_deps()"
  }

  case class Parser(content: String) {
    private val ArtifactFilter = "artifact\\s*?=\\s*?\"(.+?)\"".r("artifact")

    def allMavenCoordinates: Set[Coordinates] = {
      splitToStringsWithMavenJarsInside(content).flatMap(parseCoordinates).toSet
    }

    private def parseCoordinates(jar: String) = {
      ArtifactFilter.findFirstMatchIn(jar)
        .map(_.group("artifact"))
        .map(Coordinates.deserialize)
    }

  }

  private def splitToStringsWithMavenJarsInside(thirdPartyRepos: String) =
    for (m <- GeneralWorkspaceRuleRegex.findAllMatchIn(thirdPartyRepos)) yield m.group(0)

  private val GeneralWorkspaceRuleRegex = regexOfWorkspaceRuleWithNameMatching(".+?")

  private def regexOfWorkspaceRuleWithNameMatching(pattern: String) =
    ("""(?s)if native\.existing_rule\("""" + pattern + """"\) == None:\s*?[^\s]+"""
      + """\(\s*?name\s*?=\s*?"""" + pattern + """",[\s#]*?artifact.*?\)""").r

  private def regexOfLoadRuleWithNameMatching(pattern: String, destinationPackage: DestinationPackage) =
    ("""(?s)load\("""" + destinationPackage.bazelPackage + """/""" + pattern + """.bzl", """ + pattern + """_deps = "dependencies"\)""").r

  private def regexOfImportExternalTargetsFileMethodCall(groupIdForBazel: String) = {
    (s"  ${groupIdForBazel}_deps\\(\\)").r
  }

}
