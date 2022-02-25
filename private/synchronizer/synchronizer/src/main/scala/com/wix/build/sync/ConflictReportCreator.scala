package com.wix.build.sync

import com.wix.build.maven.{Coordinates, _}
import org.slf4j.LoggerFactory

case class ConflictReportCreator() {
  private val log = LoggerFactory.getLogger(getClass)

  def report(diffResult: DiffResult): UserAddedDepsConflictReport = {
    log.debug(s"result: updatedLocalNodes count: ${diffResult.updatedBazelLocalNodes.size}. localNodes count: ${diffResult.preExistingLocalNodes.size}. managedNodes count: ${diffResult.managedNodes.size}")

    val updatedLocalDeps = diffResult.updatedBazelLocalNodes.map(_.baseDependency)
    val localDeps = diffResult.preExistingLocalNodes.map(_.baseDependency)
    val managedDeps = diffResult.managedNodes.map(_.baseDependency)

    val conflictingLocalDeps = updatedLocalDeps.diff(localDeps)
    log.debug(s"conflictingLocalDeps count: ${conflictingLocalDeps.size}")

    UserAddedDepsConflictReport(
      higherVersionsThanBefore(localDeps, conflictingLocalDeps),
      diffManagedVersions(managedDeps, conflictingLocalDeps))
  }


  private def higherVersionsThanBefore(localDeps: Set[Dependency], conflictingLocalDeps: Set[Dependency]) = {
    intersectArtifactsIgnoringVersions(conflictingLocalDeps, localDeps)
      .filter(pair => {
        val (updatedLocal, oldLocal) = pair
        updatedLocal.version > oldLocal.version
      })
      .map(pair => {
        val (updatedLocal, oldLocal) = pair
        HigherVersionThanBefore(updatedLocal.coordinates, oldLocal.version)
      })
  }

  private def diffManagedVersions(managedDeps: Set[Dependency], conflictingLocalDeps: Set[Dependency]) = {
    val conflictingWithManagedDeps = conflictingLocalDeps.diff(managedDeps)
    log.debug(s"conflictingWithManagedDeps count: ${conflictingWithManagedDeps.size}")
    intersectArtifactsIgnoringVersions(conflictingWithManagedDeps, managedDeps)
      .map(pair => {
        val (local, managed) = pair
        DifferentManagedVersionExists(local, managed)
      })
  }

  private def intersectArtifactsIgnoringVersions(setA: Set[Dependency], setB: Set[Dependency]): Set[(Dependency, Dependency)] = {
    setA.flatMap(a => {
      val artifactB = setB.find(b => a.equalsOnCoordinatesIgnoringVersion(b))
      artifactB.map(concreteB => (a, concreteB))
    })
  }
}

case class UserAddedDepsConflictReport(higherVersionConflicts: Set[HigherVersionThanBefore] = Set(),
                                       differentManagedVersionConflicts: Set[DifferentManagedVersionExists] = Set())

case class HigherVersionThanBefore(coordinates: Coordinates, oldVersion: String) {
  override def toString: String = {
    s"previous version of ${coordinates.serialized} was $oldVersion. make sure there was no breaking api change."
  }
}

case class DifferentManagedVersionExists(localDependency: Dependency, managedDependency: Dependency) {
  override def toString: String = {
    s"local: ${localDependency.coordinates.serialized} (${localDependency.exclusions.size} exclusions). managed version: ${managedDependency.version} (${managedDependency.exclusions.size} excl.). consider undoing this change."
  }
}