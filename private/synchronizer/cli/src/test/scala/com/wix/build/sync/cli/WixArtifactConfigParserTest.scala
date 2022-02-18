package com.wix.build.sync.cli

import com.wix.build.sync.core.WixArtifactConfig
import org.specs2.mutable.SpecWithJUnit

class WixArtifactConfigParserTest extends SpecWithJUnit {
  "parses direct transitive deps" in {
    val json =
      """
        |[
        |  {
        |      "group": "some-group-id",
        |      "artifact": "some-artifact-id",
        |      "flattenTransitiveDeps": true,
        |      "ignored": "must-ignore-unknown-values"
        |  }
        |]
        |""".stripMargin

    val expectedConfig = WixArtifactConfig(
      group = "some-group-id",
      artifact = "some-artifact-id",
      flattenTransitiveDeps = true,
    )

    WixArtifactConfigParser.parse(json) mustEqual Set(expectedConfig)
  }

  "parses aliases" in {
    val json =
      """
        |[
        |  {
        |      "group": "some-group-id",
        |      "artifact": "some-artifact-id",
        |      "aliases": [
        |          "some-alias"
        |      ]
        |  }
        |]
        |""".stripMargin

    val expectedConfig = WixArtifactConfig(
      group = "some-group-id",
      artifact = "some-artifact-id",
      aliases = Set("some-alias")
    )

    WixArtifactConfigParser.parse(json) mustEqual Set(expectedConfig)
  }

  "parses tags" in {
    val json =
      """
        |[
        |  {
        |      "group": "some-group-id",
        |      "artifact": "some-artifact-id",
        |      "tags": [
        |          "some-tag"
        |      ]
        |  }
        |]
        |""".stripMargin

    val expectedConfig = WixArtifactConfig(
      group = "some-group-id",
      artifact = "some-artifact-id",
      tags = Set("some-tag")
    )

    WixArtifactConfigParser.parse(json) mustEqual Set(expectedConfig)
  }

}
