package com.wix.build.maven.mapper

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonTypeInfo}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "__class")
trait TypeAddingMixin

@JsonIgnoreProperties(Array("archive"))
trait IgnoringIsArchiveDefMixin

@JsonIgnoreProperties(Array("war"))
trait IgnoringIsWarDefMixin

@JsonIgnoreProperties(Array("protoArtifact"))
trait IgnoringIsProtoArtifactDefMixin
