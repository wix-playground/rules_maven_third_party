package com.wix.build.bazel

import com.wix.build.bazel.ImportExternalTargetsFile.findTargetWithSameNameAs
import com.wix.build.bazel.ImportExternalTargetsFileReader._
import com.wix.build.bazel.ImportExternalTargetsFileWriter.removeHeader
import com.wix.build.maven._

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object ImportExternalTargetsFile {

  def findTargetWithSameNameAs(name: String, within: String): Option[Match] = {
    regexOfImportExternalRuleWithNameMatching(name).findFirstMatchIn(within)
  }
}

case class ImportExternalTargetsFile(importExternalLoadStatement: ImportExternalLoadStatement, localWorkspace: BazelLocalWorkspace) {
  val headersAppender: HeadersAppender = HeadersAppender(importExternalLoadStatement)

  def persistTarget(ruleToPersist: RuleToPersist): Unit = {
    ruleToPersist.rule match {
      case rule: ImportExternalRule =>
        persistTargetAndCleanHeaders(rule)
      case _ =>
    }
  }

  def persistTargetAndCleanHeaders(ruleToPersist: ImportExternalRule): Unit = {
    val thirdPartyGroup = ImportExternalRule.ruleLocatorFrom(Coordinates.deserialize(ruleToPersist.artifact))
    val importTargetsFileContent =
      localWorkspace.thirdPartyImportTargetsFileContent(thirdPartyGroup).getOrElse("")
    val importTargetsFileWriter = ImportExternalTargetsFileWriter(importTargetsFileContent).withTarget(ruleToPersist)
    val newContent = importTargetsFileWriter.content

    val withSingleHeader = headersAppender.updateHeadersFor(newContent)
    localWorkspace.overwriteThirdPartyImportTargetsFile(thirdPartyGroup, withSingleHeader.content)
  }

  def deleteTarget(coordsToDelete: Coordinates, localWorkspace: BazelLocalWorkspace): Unit = {
    val thirdPartyGroup = ImportExternalRule.ruleLocatorFrom(coordsToDelete)
    val importTargetsFileContent = localWorkspace.thirdPartyImportTargetsFileContent(thirdPartyGroup)
    importTargetsFileContent.foreach { content =>
      val importTargetsFileWriter = ImportExternalTargetsFileWriter(content).withoutTarget(coordsToDelete)
      localWorkspace.overwriteThirdPartyImportTargetsFile(thirdPartyGroup, importTargetsFileWriter.content)
    }
  }

}

case class ImportExternalLoadStatement(importExternalRulePath: String,
                                       importExternalMacroName: String,
                                       mavenArchiveMacroName: String = "maven_archive") {
  val loadStatement = s"""load("$importExternalRulePath", import_external = "$importExternalMacroName", maven_archive = "$mavenArchiveMacroName")"""
}

case class HeadersAppender(importExternalLoadStatement: ImportExternalLoadStatement) {
  final val fileHeader: String =
    s"""${importExternalLoadStatement.loadStatement}
       |
       |def dependencies():""".stripMargin

  def updateHeadersFor(content: String): ImportExternalTargetsFileWriter = {
    val withNoHeaders = removeHeader(content).dropWhile(_.isWhitespace)
    ImportExternalTargetsFileWriter(
      s"""$fileHeader
         |    $withNoHeaders""".stripMargin)
  }
}

object NewLinesParser {

  def findFlexibleStartOfContent(content: String, matched: Match): String = {
    val contentStartPlusSpaces = content.take(matched.start)
    val indexOfNewLine = contentStartPlusSpaces.lastIndexOf("\n")
    contentStartPlusSpaces.take(indexOfNewLine + 1)
  }

  def removeMatched(thirdPartyRepos: String, matched: Regex.Match): String = {
    val contentStart = findFlexibleStartOfContent(thirdPartyRepos, matched)
    val contentEnd = thirdPartyRepos.drop(matched.end).dropAllPrefixNewlines
    val contentAfterRemoval = contentStart + contentEnd
    contentAfterRemoval
  }

  implicit class NewLinesParser(val s: String) {
    def containsOnlyNewLinesOrWhitespaces: Boolean = {
      s.dropWhile(_.isWhitespace).isEmpty
    }

    def dropAllPrefixNewlines: String = {
      s.dropWhile(String.valueOf(_).equals("\n"))
    }
  }

}

object ImportExternalTargetsFileReader {
  def parseCoordinates(jar: String): Option[ValidatedCoordinates] = {
    ArtifactFilter.findFirstMatchIn(jar)
      .map(_.group("artifact"))
      .map(Coordinates.deserialize)
      .map(c => ValidatedCoordinates(c, extractChecksum(jar), None, Set.empty))
      .map(vc => vc.copy(srcChecksum = extractSrcChecksum(jar)))
      .map(vc => vc.copy(tags = extractListByAttribute(TagsFilter, jar)))
  }

  def parseExtendedCoordinates(jar: String): Option[ExtendedValidatedCoordinates] = {
    ArtifactFilter.findFirstMatchIn(jar)
      .map(_.group("artifact"))
      .map(Coordinates.deserialize)
      .map(c => ExtendedValidatedCoordinates(c, extractChecksum(jar), None, Set.empty, Set.empty, Set.empty))
      .map(vc => vc.copy(srcChecksum = extractSrcChecksum(jar)))
      .map(vc => vc.copy(tags = extractListByAttribute(TagsFilter, jar)))
      .map(vc => vc.copy(deps = extractListByAttribute(CompileTimeDepsFilter, jar)))
      .map(vc => vc.copy(runtimeDeps = extractListByAttribute(RunTimeDepsFilter, jar)))
  }

  def splitToStringsWithJarImportsInside(thirdPartyRepos: String): Iterator[String] =
    for (m <- GeneralWorkspaceRuleRegex.findAllMatchIn(thirdPartyRepos)) yield m.group(0)

  private val GeneralWorkspaceRuleRegex = regexOfImportExternalRuleWithNameMatching(".+?")

  def extractArtifact(ruleText: String): String = {
    val maybeMatch = ArtifactFilter.findFirstMatchIn(ruleText)
    maybeMatch.map(_.group("artifact")).getOrElse("")
  }

  def extractChecksum(ruleText: String): Option[String] = {
    val maybeMatch = JarSha256Filter.findFirstMatchIn(ruleText)
    val stillMaybeMatch = maybeMatch.fold(ArtifactSha256Filter.findFirstMatchIn(ruleText))(m => Option(m))
    stillMaybeMatch.map(_.group("checksum"))
  }

  def extractListByAttribute(filter: Regex, ruleText: String): Set[String] = {
    val bracketsContentOrEmpty = filter.findFirstMatchIn(ruleText).map(_.group(BracketsContentGroup)).getOrElse("")
    listOfStringsFilter.findAllMatchIn(bracketsContentOrEmpty).map(_.group(StringsGroup)).toSet
  }

  def extractExclusions(ruleText: String): Set[Exclusion] = {
    ExclusionsFilter
      .findAllMatchIn(ruleText)
      .map(m => Exclusion(m.group("groupId"), m.group("artifactId")))
      .toSet
  }

  def parseImportExternalDep(text: String): Option[String] = {
    val maybeMatch = ImportExternalDepDeprecateFilter.findFirstMatchIn(text)
    val stillMaybeMatch = maybeMatch.fold(ImportExternalDepFilter.findFirstMatchIn(text))(m => Option(m))
    stillMaybeMatch.map(_.group("ruleName"))
  }

  def parseImportExternalName(ruleText: String): Option[String] = {
    val maybeMatch = NameFilter.findFirstMatchIn(ruleText)
    maybeMatch.map(_.group("name"))
  }

  def extractNeverlink(ruleText: String): Boolean = {
    val maybeMatch = NeverlinkFilter.findFirstMatchIn(ruleText)
    maybeMatch.map(_.group("neverlink")).contains("1")
  }

  def extractTestOnly(ruleText: String): Boolean = {
    val maybeMatch = TestOnlyFilter.findFirstMatchIn(ruleText)
    maybeMatch.map(_.group("testonly_")).contains("1")
  }

  private def extractsSnapshotSources(ruleText: String) = {
    val maybeMatch = SnapshotSourcesFilter.findFirstMatchIn(ruleText)
    maybeMatch.map(_.group("snapshot_sources")).contains("1")
  }

  private def extractSrcChecksum(ruleText: String) = {
    val maybeMatch = SrcSha256Filter.findFirstMatchIn(ruleText)
    maybeMatch.map(_.group("src_checksum"))
  }


  def regexOfImportExternalRuleWithNameMatching(pattern: String): Regex = {
    ("(?s)([^\\s]+)" + """\(\s*?name\s*?=\s*?"""" + pattern + """",[\s#]*?(?:artifact|testonly_).*?\)""").r
  }

  val RegexOfAnyLoadStatement: Regex = """load\(.*\)""".r

  def wixSnapshotHeaderExists(content: String): Boolean =
    RegexOfAnyLoadStatement.findAllIn(content).exists(_.contains("wix_snapshot_scala_maven_import_external"))

  val NameFilter: Regex = """(?s)name\s*?=\s*?"(.+?)"""".r("name")
  val ArtifactFilter: Regex = """(?s)artifact\s*?=\s*?"(.+?)"""".r("artifact")
  val BracketsContentGroup = "bracketsContent"
  val ExportsFilter: Regex = """(?s)exports\s*?=\s*?\[(.+?)]""".r(BracketsContentGroup)
  val RunTimeDepsFilter: Regex = """(?s)runtime_deps\s*?=\s*?\[(.+?)]""".r(BracketsContentGroup)
  val CompileTimeDepsFilter: Regex = """(?s)\n\s*?deps\s*?=\s*?\[(.+?)]""".r(BracketsContentGroup)
  val ExclusionsFilter: Regex = """(?s)\n\s*?excludes\s*?=\s*?\[(.+?)]""".r(BracketsContentGroup)

  val StringsGroup = "Strings"
  val listOfStringsFilter: Regex = """"(.+?)"""".r(StringsGroup)
  val JarSha256Filter: Regex = """(?s)\sjar_sha256\s*?=\s*?"(.+?)"""".r("checksum")
  val ArtifactSha256Filter: Regex = """(?s)artifact_sha256\s*?=\s*?"(.+?)"""".r("checksum")
  val ImportExternalDepDeprecateFilter: Regex = """@(.*?)//.*""".r("ruleName")
  val ImportExternalDepFilter: Regex = """@(.*)""".r("ruleName")

  val SrcSha256Filter: Regex = """(?s)srcjar_sha256\s*?=\s*?"(.+?)"""".r("src_checksum")
  val SnapshotSourcesFilter: Regex = """(?s)snapshot_sources\s*=\s*([0-1])""".r("snapshot_sources")
  val NeverlinkFilter: Regex = """(?s)neverlink\s*=\s*([0-1])""".r("neverlink")
  val TestOnlyFilter: Regex = """(?s)testonly_\s*=\s*([0-1])""".r("testonly_")
  val TagsFilter: Regex = """(?s)tags\s*?=\s*?\[(.+?)\]""".r(BracketsContentGroup)

}

case class ImportExternalTargetsFileReader(content: String) {
  def allMavenCoordinates: Set[ValidatedCoordinates] = {
    val strings = splitToStringsWithJarImportsInside(content)
    strings.flatMap(parseCoordinates).toSet
  }

  def allMavenExtendedCoordinates: Set[ExtendedValidatedCoordinates] = {
    val strings = splitToStringsWithJarImportsInside(content)
    strings.flatMap(parseExtendedCoordinates).toSet
  }

  def ruleByName(name: String): Option[ImportExternalRule] =
    findTargetWithSameNameAs(name = name, within = content)
      .map(extractFullMatchText)
      .flatMap(parseTargetText(name))

  private def extractFullMatchText(aMatch: Match): String = aMatch.group(0)

  private def parseTargetText(ruleName: String)(ruleText: String): Option[ImportExternalRule] = {
    val someRule = Some(new ImportExternalRule(
      name = ruleName,
      artifact = extractArtifact(ruleText),
      exports = extractListByAttribute(ExportsFilter, ruleText),
      runtimeDeps = extractListByAttribute(RunTimeDepsFilter, ruleText),
      compileTimeDeps = extractListByAttribute(CompileTimeDepsFilter, ruleText),
      exclusions = extractListByAttribute(ExclusionsFilter, ruleText),
      checksum = extractChecksum(ruleText),
      srcChecksum = extractSrcChecksum(ruleText),
      snapshotSources = extractsSnapshotSources(ruleText),
      neverlink = extractNeverlink(ruleText),
      testOnly = extractTestOnly(ruleText)))
    someRule
  }


  def parseTargetTextAndName(ruleText: String): Option[ImportExternalRule] = {
    parseTargetText(parseImportExternalName(ruleText).get)(ruleText)
  }

  def findCoordinatesByName(name: String): Option[ValidatedCoordinates] = {
    findTargetWithSameNameAs(name = name, within = content)
      .map(extractFullMatchText)
      .flatMap(parseCoordinates)
  }
}

case class AllImportExternalFilesCoordinatesReader(filesContent: Set[String]) {
  def allMavenCoordinates: Set[ValidatedCoordinates] = {
    filesContent.flatMap(c => ImportExternalTargetsFileReader(c).allMavenCoordinates)
  }
}

case class ValidatedCoordinates(coordinates: Coordinates,
                                checksum: Option[String], srcChecksum: Option[String],
                                tags: Set[String])

case class ExtendedValidatedCoordinates(coordinates: Coordinates,
                                        checksum: Option[String], srcChecksum: Option[String],
                                        tags: Set[String],
                                        deps: Set[String],
                                        runtimeDeps: Set[String])