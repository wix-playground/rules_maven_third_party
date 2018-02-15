package com.wix.build.maven

trait MavenDependencyResolver {

  def managedDependenciesOf(artifact: Coordinates): Set[Dependency]

  def dependencyClosureOf(baseDependencies: Set[Dependency], withManagedDependencies: Set[Dependency]): Set[DependencyNode]

  def directDependenciesOf(artifact: Coordinates): Set[Dependency]

  def allDependenciesOf(artifact: Coordinates): Set[Dependency] = {
    val directDependencies = directDependenciesOf(artifact)
    dependencyClosureOf(directDependencies,managedDependenciesOf(artifact)).map(_.baseDependency)
  }

  protected def validatedDependency(dependency: Dependency): Dependency = {
    import dependency.coordinates._
    if (
      foundTokenIn(groupId) ||
        foundTokenIn(artifactId) ||
        foundTokenIn(version) ||
        packaging.exists(foundTokenIn) ||
        classifier.exists(foundTokenIn)
    ) throw new PropertyNotDefinedException(dependency)
    dependency
  }

  private def foundTokenIn(value: String): Boolean = value.contains("$")

}

