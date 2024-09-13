package com.wix.build.bazel

case class MissingArtifactsException(missingArtifactsCoordinates: Set[String])
  extends RuntimeException(s"Missing artifacts: \n${missingArtifactsCoordinates.mkString("\n")}")
