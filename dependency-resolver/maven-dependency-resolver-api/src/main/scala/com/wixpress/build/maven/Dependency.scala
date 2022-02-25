package com.wix.build.maven

case class Dependency(coordinates: Coordinates, scope: MavenScope, exclusions: Set[Exclusion] = Set.empty) {

  def withExclusions(exclusions: Set[Exclusion]): Dependency = this.copy(exclusions = exclusions)

  def version: String = this.coordinates.version

  def withVersion(version: String): Dependency = this.copy(coordinates = this.coordinates.copy(version = version))

  def withScope(scope:MavenScope) : Dependency = this.copy(scope = scope)
}
