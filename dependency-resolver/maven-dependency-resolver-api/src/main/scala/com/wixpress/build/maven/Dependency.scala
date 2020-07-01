package com.wix.build.maven

case class Dependency(coordinates: Coordinates,
                      scope: MavenScope,
                      isNeverLink: Boolean = false,
                      exclusions: Set[Exclusion] = Set.empty,
                      aliases: Set[String] = Set.empty,
                      tags: Set[String] = Set.empty) {

  def withExclusions(exclusions: Set[Exclusion]): Dependency = copy(exclusions = exclusions)

  def withAliases(aliases: Set[String]): Dependency = copy(aliases = aliases)

  def withTags(tags: Set[String]): Dependency = copy(tags = tags)

  def version: String = coordinates.version

  def withVersion(version: String): Dependency = copy(coordinates = coordinates.copy(version = version))

  def withScope(scope: MavenScope): Dependency = copy(scope = scope)

  def withIsNeverLink(isNeverLink: Boolean): Dependency = copy(isNeverLink = isNeverLink)

  def equalsOnCoordinatesIgnoringVersion(dependency: Dependency): Boolean = dependency.coordinates.equalsIgnoringVersion(coordinates)

  def equalsIgnoringNeverlink(dependency: Dependency): Boolean = {
    this.coordinates == dependency.coordinates &&
      this.scope == dependency.scope &&
      this.exclusions == dependency.exclusions
  }

  def shortSerializedForm() = s"${coordinates.groupId}:${coordinates.artifactId}"
}

object Dependency {

  implicit class DependenciesExtended(dependencies: List[Dependency]) {
    def forceCompileScope: List[Dependency] = dependencies.map(_.forceCompileScope)
  }

  implicit class DependenciesSetExtended(dependencies: Set[Dependency]) {
    def forceCompileScope: Set[Dependency] = dependencies.map(_.forceCompileScope)
  }

  implicit class DependencyExtended(dependency: Dependency) {
    def forceCompileScope: Dependency = dependency.copy(scope = MavenScope.Compile)
  }

}
