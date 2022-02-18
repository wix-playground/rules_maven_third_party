package com.wix.build.sync.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class WixArtifactConfig(group: String,
                             artifact: String,
                             flattenTransitiveDeps: Boolean = false,
                             aliases: Set[String] = Set.empty,
                             tags: Set[String] = Set.empty)
