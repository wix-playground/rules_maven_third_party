package com.wix.build.maven

case class Dependency(coordinates: Coordinates, scope: MavenScope, isNeverLink: Boolean = false, exclusions: Set[Exclusion] = Set.empty) {

  def withExclusions(exclusions: Set[Exclusion]): Dependency = this.copy(exclusions = exclusions)

  def version: String = this.coordinates.version

  def withVersion(version: String): Dependency = this.copy(coordinates = this.coordinates.copy(version = version))

  def withScope(scope: MavenScope): Dependency = this.copy(scope = scope)

  def withIsNeverLink(isNeverLink: Boolean): Dependency = this.copy(isNeverLink = isNeverLink)

  def equalsOnCoordinatesIgnoringVersion(dependency: Dependency): Boolean = dependency.coordinates.equalsIgnoringVersion(coordinates)

  def equalsIgnoringNeverlink(dependency: Dependency): Boolean = {
    this.coordinates == dependency.coordinates &&
      this.scope == dependency.scope &&
      this.exclusions == dependency.exclusions
  }

  def shortSerializedForm() = s"${coordinates.groupId}:${coordinates.artifactId}"
}

object Dependency {
  implicit class DependenciesExtended(dependencies:List[Dependency]) {
    def forceCompileScope: List[Dependency] = dependencies.map(_.forceCompileScope)
  }

  implicit class DependenciesSetExtended(dependencies:Set[Dependency]) {
    def forceCompileScope: Set[Dependency] = dependencies.map(_.forceCompileScope)
  }

  implicit class DependencyExtended(dependency:Dependency) {
    def forceCompileScope: Dependency = dependency.copy(scope = MavenScope.Compile)
  }
}