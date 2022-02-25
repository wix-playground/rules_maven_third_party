package com.wix.build.maven

import com.wix.build.maven._

import scala.annotation.tailrec

class FilteringGlobalExclusionDependencyResolver(resolver: MavenDependencyResolver, globalExcludes: Set[Coordinates]) extends MavenDependencyResolver {

  val filterer = new GlobalExclusionFilterer(globalExcludes)

  override def managedDependenciesOf(artifact: Coordinates): List[Dependency] = resolver.managedDependenciesOf(artifact)

  override def dependencyClosureOf(baseDependencies: List[Dependency],
                                   withManagedDependencies: List[Dependency],
                                   ignoreMissingDependenciesFlag: Boolean = true): Set[DependencyNode] =
    filterer.filterGlobalsFromDependencyNodes(resolver.dependencyClosureOf(baseDependencies, withManagedDependencies))

  override def directDependenciesOf(coordinates: Coordinates): List[Dependency] =
    filterGlobalsFromDependencies(coordinates, resolver.directDependenciesOf(coordinates)).distinct

  private def filterGlobalsFromDependencies(coordinates: Coordinates,
                                            dependencies: List[Dependency]): List[Dependency] = {

    val excludedDependencies = dependencies.filter(excluded)
    val nodes: Set[DependencyNode] = if (excludedDependencies.isEmpty) {
      Set.empty
    } else {
      val managedDependencies = resolver.managedDependenciesOf(coordinates)
      resolver.dependencyClosureOf(excludedDependencies, managedDependencies)
    }
    filterer.filterGlobalsFromDependencies(dependencies, nodes).distinct
  }

  private def excluded(dependency: Dependency) =
    globalExcludes.exists(dependency.coordinates.equalsOnGroupIdAndArtifactId)
}

class GlobalExclusionFilterer(globalExcludes: Set[Coordinates]) {

  def filterGlobalsFromDependencyNodes(dependencyNodes: Set[DependencyNode]): Set[DependencyNode] =
    dependencyNodes
      .filterNot(dependencyNode => excluded(dependencyNode.baseDependency))
      .map(filterGlobalsFromDependencies(dependencyNodes))

  private def filterGlobalsFromDependencies(dependencyNodes: Set[DependencyNode])(depNode: DependencyNode): DependencyNode =
    depNode.copy(dependencies = filterGlobalsFromDependencies(depNode.dependencies.toList, dependencyNodes).toSet)

  @tailrec
  final def filterGlobalsFromDependencies(dependencies: List[Dependency],
                                          dependencyGraph: Set[DependencyNode]): List[Dependency] = {
    val (excludedDependencies, includedDependencies) = dependencies.partition(excluded)
    if (excludedDependencies.isEmpty) {
      includedDependencies
    } else {
      val transitiveDependencies = excludedDependencies
        .flatMap(retainTransitiveDependencies(dependencyGraph))
        .map(preferOriginalVersionAsFoundIn(includedDependencies))

      filterGlobalsFromDependencies(includedDependencies ++ transitiveDependencies, dependencyGraph)
    }
  }

  private def preferOriginalVersionAsFoundIn(previouslyFoundDependencies: List[Dependency])(newlyFoundDependency: Dependency) = {
    val resolvedVersion = previouslyFoundDependencies
      .find(_.coordinates.equalsIgnoringVersion(newlyFoundDependency.coordinates))
      .map(_.version)
      .getOrElse(newlyFoundDependency.coordinates.version)
    newlyFoundDependency.withVersion(resolvedVersion)
  }

  private def retainTransitiveDependencies(dependencyGraph: Set[DependencyNode])(excluded: Dependency): Set[Dependency] = {
    val originalScope = excluded.scope
    // hard assumption that excluded dependency is in the given nodes
    dependencyGraph
      .find(_.baseDependency.coordinates.equalsIgnoringVersion(excluded.coordinates))
      .getOrElse(throw new RuntimeException(s"Could not find dependency node for the excluded ${excluded.coordinates}"))
      .dependencies
      .map(updateDependencyScopeAccordingTo(originalScope))
  }

  private def updateDependencyScopeAccordingTo(originalScope: MavenScope)(transitive: Dependency) = {
    val transitiveScope = transitive.scope
    val newScope = (originalScope, transitiveScope) match {
      case (MavenScope.Compile, MavenScope.Runtime) => MavenScope.Runtime
      case (scope, _) => scope
    }
    transitive.copy(scope = newScope)
  }

  private def excluded(dependency: Dependency) =
    globalExcludes.exists(dependency.coordinates.equalsOnGroupIdAndArtifactId)
}