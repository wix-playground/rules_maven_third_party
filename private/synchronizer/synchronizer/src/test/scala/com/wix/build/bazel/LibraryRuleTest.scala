package com.wix.build.bazel

import com.wix.build.bazel.LibraryRule.ScalaLibraryRuleType
import org.specs2.mutable.SpecificationWithJUnit

//noinspection TypeAnnotation
class LibraryRuleTest extends SpecificationWithJUnit {
  "LibraryRule" should {
    "serialize rule with no attributes" in {
      val rule = LibraryRule(name = "name")

      rule.serialized must beEqualIgnoringSpaces(
        """scala_import(
          |    name = "name",
          |    tags = [
          |        "manual",
          |        "no-index",
          |    ],
          |)""".stripMargin)
    }

    "serialize rule with scala_library rule type" in {
      val rule = LibraryRule(name = "name", libraryRuleType = ScalaLibraryRuleType)
      rule.serialized must beEqualIgnoringSpaces(
        """scala_library(
          |    name = "name",
          |    tags = [
          |        "manual",
          |        "no-index",
          |    ],
          |)""".stripMargin)
    }

    "serialize rule with data attribute" in {
      val rule = LibraryRule(name = "name", data = Set("some-data"))

      rule.serialized must containIgnoringSpaces(
        """data = [
          |   "some-data",
          |]""".stripMargin)
    }

    "serialize rule jar" in {
      val rule = LibraryRule(name = "name", jars = Set("@jar_reference"))

      rule.serialized must containIgnoringSpaces(
        """jars = [
          |    "@jar_reference",
          |]""".stripMargin)
    }

    "serialize rule compile time dependency" in {
      val rule = LibraryRule(
        name = "name",
        compileTimeDeps = Set("some-compile-time-dep")
      )

      rule.serialized must containIgnoringSpaces(
        """deps = [
          |    "some-compile-time-dep",
          |]""".stripMargin)
    }

    "serialize rule runtime dependency" in {
      val rule = LibraryRule(
        name = "name",
        runtimeDeps = Set("some-runtime-dep")
      )

      rule.serialized must containIgnoringSpaces(
        """runtime_deps = [
          |    "some-runtime-dep",
          |]""".stripMargin)
    }

    "serialize rule with exports" in {
      val rule = LibraryRule(
        name = "name",
        exports = Set("some-export")
      )

      rule.serialized must containIgnoringSpaces(
        """exports = [
          |    "some-export",
          |],""".stripMargin)
    }

    "serialize rule with exclude" in {
      val rule = LibraryRule(
        name = "name",
        exclusions = Set("excluded.group:excluded-artifact")
      )

      rule.serialized must containIgnoringSpaces(
        """excludes = [
          |    "excluded.group:excluded-artifact",
          |],""".stripMargin)
    }

    "serialize rule with multiple excludes" in {
      val rule = LibraryRule(
        name = "name",
        exclusions = Set("excluded.group:excluded-artifact", "excluded.group:excluded-artifact2")
      )

      rule.serialized must containIgnoringSpaces(
        """excludes = [
          |    "excluded.group:excluded-artifact",
          |    "excluded.group:excluded-artifact2",
          |],""".stripMargin)
    }

    "serialize rule with multiple dependencies" in {
      val rule = LibraryRule(
        name = "name",
        compileTimeDeps = Set("dep3", "dep1", "dep2")
      )

      rule.serialized must containIgnoringSpaces(
        """deps = [
          |    "dep1",
          |    "dep2",
          |    "dep3",
          |],""".stripMargin)
    }

    "serialize rule with testonly" in {
      val rule = LibraryRule(
        name = "name",
        testOnly = true
      )

      rule.serialized must containIgnoringSpaces(
        """testonly_ = 1,""".stripMargin)
    }

    "not serialize testonly for rules that do not need it" in {
      val rule = LibraryRule(
        name = "name"
      )

      rule.serialized must not(containIgnoringSpaces(
        """testonly_ = 1,""".stripMargin))
    }
  }

  private def containIgnoringSpaces(target: String) = ((_: String).trimSpaces) ^^ contain(target.trimSpaces)

  private def beEqualIgnoringSpaces(target: String) = ((_: String).trimSpaces) ^^ beEqualTo(target.trimSpaces)

  implicit class StringExtended(string: String) {
    def trimSpaces: String = string.replaceAll(" +", " ").replaceAll("(?m)^ ", "")
  }

}
