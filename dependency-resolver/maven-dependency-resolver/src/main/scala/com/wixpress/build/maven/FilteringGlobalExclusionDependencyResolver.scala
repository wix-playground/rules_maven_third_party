package com.wix.build.maven

import scala.annotation.tailrec

class FilteringGlobalExclusionDependencyResolver(resolver: MavenDependencyResolver, globalExcludes: Set[Coordinates]) extends MavenDependencyResolver {

  override def managedDependenciesOf(artifact: Coordinates): Set[Dependency] = resolver.managedDependenciesOf(artifact)


  override def dependencyClosureOf(baseDependencies: Set[Dependency], withManagedDependencies: Set[Dependency]): Set[DependencyNode] =
    filterGlobalsFromDependencyNodes(resolver.dependencyClosureOf(baseDependencies, withManagedDependencies))

  private def filterGlobalsFromDependencyNodes(dependencyNodes: Set[DependencyNode]): Set[DependencyNode] = {
    dependencyNodes.filterNot(dependencyNode => excluded(dependencyNode.baseDependency))
      .map(filterGlobalsFromDependencies(dependencyNodes))
  }

  private def filterGlobalsFromDependencies(dependencyNodes: Set[DependencyNode])(depNode: DependencyNode): DependencyNode =
    depNode.copy(dependencies = filterGlobalsFromDependencies(depNode.dependencies, dependencyNodes))


  private def preferOriginalVersionAsFoundIn(previouslyFoundDependencies: Set[Dependency])(newlyFoundDependency: Dependency) = {
    val resolvedVersion = previouslyFoundDependencies
      .find(_.coordinates.equalsIgnoringVersion(newlyFoundDependency.coordinates))
      .map(_.version)
      .getOrElse(newlyFoundDependency.coordinates.version)
    newlyFoundDependency.withVersion(resolvedVersion)
  }

  @tailrec
  private def filterGlobalsFromDependencies(dependencies: Set[Dependency], dependencyGraph: Set[DependencyNode]): Set[Dependency] = {
    val (excludedDependencies, includedDependencies) = dependencies.partition(excluded)
    if (excludedDependencies.isEmpty)
      includedDependencies
    else {
      val transitiveDependencies = excludedDependencies
        .flatMap(retainTransitiveDependencies(dependencyGraph))
        .map(preferOriginalVersionAsFoundIn(includedDependencies))

      filterGlobalsFromDependencies(includedDependencies ++ transitiveDependencies, dependencyGraph)
    }
  }

  private def retainTransitiveDependencies(dependencyGraph: Set[DependencyNode])(excluded: Dependency): Set[Dependency] = {
    val originalScope = excluded.scope
    // hard assumption that excluded dependency is in the given nodes
    dependencyGraph.find(_.baseDependency.coordinates.equalsIgnoringVersion(excluded.coordinates))
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

  private def filterGlobalsFromDependencies(coordinates: Coordinates, dependencies: Set[Dependency]): Set[Dependency] = {
    val excludedDependencies = dependencies.filter(excluded)
    val nodes: Set[DependencyNode] = if (excludedDependencies.isEmpty) Set.empty else {
      val managedDependencies = resolver.managedDependenciesOf(coordinates)
      resolver.dependencyClosureOf(excludedDependencies, managedDependencies)
    }
    filterGlobalsFromDependencies(dependencies, nodes)
  }

  override def directDependenciesOf(coordinates: Coordinates): Set[Dependency] = {
    filterGlobalsFromDependencies(coordinates, resolver.directDependenciesOf(coordinates))
  }

  private def excluded(dependency: Dependency) = {
    globalExcludes.exists(dependency.coordinates.equalsOnGroupIdAndArtifactId)
  }
}
