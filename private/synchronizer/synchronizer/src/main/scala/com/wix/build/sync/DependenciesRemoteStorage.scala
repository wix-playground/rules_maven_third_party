package com.wix.build.sync

import com.wix.build.maven.{Coordinates, _}

trait DependenciesRemoteStorage {
  def checksumFor(node: DependencyNode): Option[String]
}

object ArtifactoryRemoteStorage {
  type Sha256 = String

  implicit class GroupIdConvertors(groupId: String) {
    def toPath: String = {
      groupId.replace(".", "/")
    }
  }

  implicit class CoordinatesConverters(coordinates: Coordinates) {
    def toArtifactPath: String = {
      val groupId = coordinates.groupId.toPath
      val artifactId = coordinates.artifactId
      val version = coordinates.version
      val packaging = coordinates.packaging.value
      val classifier = coordinates.classifier.fold("")("-".concat)
      s"""$groupId/$artifactId/$version/$artifactId-$version$classifier.$packaging"""
    }

    def toSha256Path: String = {
      toArtifactPath + ".sha256"
    }
  }

  implicit class DependencyNodeExtensions(node: DependencyNode) {
    def updateChecksumFrom(dependenciesRemoteStorage: DependenciesRemoteStorage): BazelDependencyNode = {

      val maybeChecksum = if (node.isSnapshot) None else
        dependenciesRemoteStorage.checksumFor(node)

      // TODO - should add a dependenciesRemoteStorage.doSourcesExist which will only check sources.jar existence
      // instead of downloading the whole jar to calculate the sha
      val maybeSrcChecksum = dependenciesRemoteStorage.checksumFor(node.asSourceNode)
      val snapshotSources = node.isSnapshot && maybeSrcChecksum.isDefined
      val maybeSrcChecksumIfRelevant = if (node.isSnapshot) None else maybeSrcChecksum

      BazelDependencyNode(
        node.baseDependency,
        node.dependencies,
        maybeChecksum,
        maybeSrcChecksumIfRelevant,
        snapshotSources = snapshotSources
      )
    }

    def asSourceNode: DependencyNode = {
      val srcCoordinates = node.baseDependency.coordinates.copy(classifier = Some("sources"))
      val srcBaseDependency = node.baseDependency.copy(coordinates = srcCoordinates)

      node.copy(baseDependency = srcBaseDependency, dependencies = Set.empty)
    }
  }

  def decorateNodesWithChecksum(closure: Set[DependencyNode])(dependenciesRemoteStorage: DependenciesRemoteStorage): Set[BazelDependencyNode] = {
    closure.par.map(_.updateChecksumFrom(dependenciesRemoteStorage)).toList.toSet
  }
}

case class ArtifactNotFoundException(message: String) extends RuntimeException(message)

case class ErrorFetchingArtifactException(message: String) extends RuntimeException(message)