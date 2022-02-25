package com.wix.build.bazel

import com.wix.build.bazel.NeverLinkResolver.globalNeverLinkDependencies
import com.wix.build.maven._

object NeverLinkResolver {
  val globalNeverLinkDependencies: Set[Coordinates] = Set(
    Coordinates("javax.servlet", "javax.servlet-api", "ignore-version"),
    Coordinates("mysql", "mysql-connector-java", "ignore-version"))

  def apply(localNeverlinkDependencies: Set[Coordinates] = Set.empty, overrideGlobalNeverLinkDependencies: Set[Coordinates] = Set.empty): NeverLinkResolver = {
    if (overrideGlobalNeverLinkDependencies.nonEmpty)
      new NeverLinkResolver(overrideGlobalNeverLinkDependencies, localNeverlinkDependencies)
    else
      new NeverLinkResolver(globalNeverLinkDependencies, localNeverlinkDependencies)
  }
}

class NeverLinkResolver(globalPotentiallyNeverLinkDependencies: Set[Coordinates] = globalNeverLinkDependencies,
                        localNeverlinkDependencies: Set[Coordinates] = Set.empty) {
  def isNeverLink(dependency: Dependency): Boolean = {
    globalPotentiallyNeverLinkDependencies.exists(dependency.coordinates.equalsIgnoringVersion) ||
      dependency.scope == MavenScope.Provided ||
      dependency.isNeverLink
  }

  def fixAllTransitiveNeverLinks(node: DependencyNode): DependencyNode =
    node.copy(dependencies = node.dependencies.map(dep => dep.copy(isNeverLink = isNeverLink(dep))))

  def isLinkable(artifact: Coordinates): Boolean = {
    globalPotentiallyNeverLinkDependencies.exists(_.equalsIgnoringVersion(artifact)) && !localNeverlinkDependencies.exists(_.equalsIgnoringVersion(artifact))
  }
}