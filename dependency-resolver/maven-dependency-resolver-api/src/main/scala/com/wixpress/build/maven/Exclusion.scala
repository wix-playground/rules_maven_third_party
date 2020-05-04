package com.wix.build.maven

case class Exclusion(groupId: String, artifactId: String) {
  def serialized: String = s"$groupId:$artifactId"

  def equalsCoordinates(coordinates: Coordinates): Boolean =
    this.groupId == coordinates.groupId && this.artifactId == coordinates.artifactId

}

object Exclusion {
  def apply(dependency: Dependency): Exclusion = Exclusion(dependency.coordinates)

  def apply(serialized: String): Exclusion = serialized.split(":") match {
    case Array(groupId) => Exclusion(groupId, "*")
    case Array(groupId, artifactId) => Exclusion(groupId, artifactId)
    case _ => throw new RuntimeException(s"Unsupported exclusion format '$serialized'")
  }

  def apply(coordinates: Coordinates): Exclusion = Exclusion(coordinates.groupId, coordinates.artifactId)
}
