package com.wix.build.bazel

import org.specs2.mutable.SpecificationWithJUnit

//noinspection TypeAnnotation
class ImportExternalRuleTest extends SpecificationWithJUnit {
  "ImportExternalRule" should {

    // TODO: wrap licenses and server_urls with macro
    // also wrap if statement in macro
    "serialize rule with default attributes" in {
      val rule = ImportExternalRule(name = "name", artifact = "artifact")

      rule.serialized must beEqualIgnoringSpaces(
        """import_external(
          |  name = "name",
          |  artifact = "artifact", # fixme: missing jar
          |)""".stripMargin)
    }

    "serialize rule compile time dependency" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        compileTimeDeps = Set("some-compile-time-dep")
      )

      rule.serialized must containIgnoringSpaces(
        """deps = [
          |   "some-compile-time-dep",
          |]""".stripMargin)
    }

    "serialize rule runtime dependency" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        runtimeDeps = Set("some-runtime-dep")
      )

      rule.serialized must containIgnoringSpaces(
        """runtime_deps = [
          |   "some-runtime-dep",
          |]""".stripMargin)
    }

    "serialize rule with exports" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        exports = Set("some-export")
      )

      rule.serialized must containIgnoringSpaces(
        """exports = [
          |   "some-export",
          |],""".stripMargin)
    }

    "serialize rule with exclude" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        exclusions = Set("excluded.group:excluded-artifact")
      )

      rule.serialized must containIgnoringSpaces(
        """excludes = [
          |   "excluded.group:excluded-artifact",
          |]""".stripMargin)
    }

    "serialize rule with multiple excludes" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        exclusions = Set("excluded.group:excluded-artifact","excluded.group:excluded-artifact2")
      )

      rule.serialized must containIgnoringSpaces(
        """excludes = [
          |   "excluded.group:excluded-artifact",
          |   "excluded.group:excluded-artifact2",
          |]""".stripMargin)
    }

    "serialize rule with multiple dependencies" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        compileTimeDeps = Set("dep3", "dep1", "dep2")
      )

      rule.serialized must containIgnoringSpaces(
        """deps = [
          |  "dep1",
          |  "dep2",
          |  "dep3",
          |],""".stripMargin)
    }

    "serialize rule with testonly_" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        testOnly = true
      )

      val serialized = rule.serialized
      serialized must containIgnoringSpaces(
        """testonly_ = 1,""".stripMargin)
    }

    "not serialize testonly_ for rules that do not need it" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
      )

      rule.serialized must not(containIgnoringSpaces(
        """testonly_ = 1,""".stripMargin))
    }

    "serialize rule with checksum" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        checksum = Some("checksum")
      )

      val serialized = rule.serialized
      serialized must containIgnoringSpaces(
        """artifact_sha256 = "checksum",""".stripMargin)
    }

    "serialize rule with src checksum" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        srcChecksum = Some("checksum")
      )

      val serialized = rule.serialized
      serialized must containIgnoringSpaces(
        """srcjar_sha256 = "checksum",""".stripMargin)
    }

    "serialize rule with neverlink" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        neverlink = true
      )

      val serialized = rule.serialized
      serialized must containIgnoringSpaces("neverlink = 1,".stripMargin)
      serialized must containIgnoringSpaces("""generated_linkable_rule_name = "linkable",""".stripMargin)
    }

    "serialize rule with alias" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        aliases = Set("alias1", "alias2"),
      )

      val serialized = rule.serialized
      serialized must containIgnoringSpaces(
        """|aliases = [
           |    "alias1",
           |    "alias2",
           |],""".stripMargin)
    }

    "serialize rule with tags" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        tags = Set("tag1", "tag2"),
      )

      val serialized = rule.serialized
      serialized must containIgnoringSpaces(
        """|tags = [
           |    "tag1",
           |    "tag2",
           |],""".stripMargin)
    }

    "serialize rule with flattened transitive closure" in {
      val rule = ImportExternalRule(
        name = "name",
        artifact = "artifact",
        transitiveClosureDeps = Set("dep1", "dep2"),
      )

      val serialized = rule.serialized
      serialized must containIgnoringSpaces(
        """transitive_closure_deps = [
          |  "dep1",
          |  "dep2",
          |],""".stripMargin)
    }
  }

  private def containIgnoringSpaces(target: String) = ((_: String).trimSpaces) ^^ contain(target.trimSpaces)

  private def beEqualIgnoringSpaces(target: String) = ((_: String).trimSpaces) ^^ beEqualTo(target.trimSpaces)

  implicit class StringExtended(string: String) {
    def trimSpaces: String = string.replaceAll(" +", " ").replaceAll("(?m)^ ", "")
  }

}
