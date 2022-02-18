package com.wix.build.sync.cli

import com.wix.build.sync.core.WixArtifactConfig
import com.wix.hoopoe.json.JsonMapper.Implicits.global
import com.wix.hoopoe.json._

object WixArtifactConfigParser {
  def parse(json: String): Set[WixArtifactConfig] = {
    val configs = json.as[Set[WixArtifactConfig]]
    configs.map(fixDefaultValues)
  }

  private def fixDefaultValues(config: WixArtifactConfig): WixArtifactConfig = {
    val aliases = if (config.aliases == null) Set.empty[String] else config.aliases
    val tags = if (config.tags == null) Set.empty[String] else config.tags

    config.copy(tags = tags, aliases = aliases)
  }
}
