package com.wix.build.maven

import scala.util.Random

object MavenMakers {

  def anExclusion(excludedArtifactId: String): Exclusion = Exclusion("some.excluded.group",excludedArtifactId)

  private val defaultArtifactPrefix = "some-artifact"

  private def randomString() = Random.alphanumeric.take(4).mkString

  def someGroupId: String = "some.group"

  def someArtifactId(
                      artifactIdPrefix: String = defaultArtifactPrefix,
                      index: Int = Random.nextInt
                    ): String =
    artifactIdPrefix + index

  def randomCoordinates(
                         withVersion: String = randomString(),
                         artifactIdPrefix: String = defaultArtifactPrefix,
                         index: Int = Random.nextInt()
                       ): Coordinates =
    Coordinates(someGroupId, someArtifactId(artifactIdPrefix, index), withVersion)

  def randomDependency(
                        withVersion: String = randomString(),
                        withScope: MavenScope = MavenScope.Compile,
                        artifactIdPrefix: String = defaultArtifactPrefix,
                        index: Int = Random.nextInt(),
                        withExclusions: Set[Exclusion] = Set.empty
                      ): Dependency =
    Dependency(randomCoordinates(withVersion, artifactIdPrefix, index), withScope, withExclusions)

  def someCoordinates(artifactId:String) = Coordinates("some.group",artifactId,"some-version")

  def aDependency(artifactId:String,scope:MavenScope = MavenScope.Compile) = Dependency(someCoordinates(artifactId),scope)

  def aRootDependencyNode(dependency: Dependency) = DependencyNode(dependency,Set.empty)
}
