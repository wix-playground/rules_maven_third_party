package com.wix.build.sync.core

import com.wix.build.maven._

object DependencyConfigAugmenter {

  def augment(deps: Set[BazelDependencyNode], managedDependencies: List[Dependency]): Set[BazelDependencyNode] = {
    val lookup = deps
      .map(node => new CoordKey(node.baseDependency.coordinates) -> node)
      .toMap

    val updatedDeps = applyConfigs(lookup, managedDependencies)

    update(in = lookup, withValues = updatedDeps).values.toSet
  }

  private def applyConfigs(lookup: Map[CoordKey, BazelDependencyNode],
                           managedDependencies: List[Dependency]): Seq[(CoordKey, BazelDependencyNode)] = {
    managedDependencies
      .flatMap { config =>
        // add support for packaging and classifier yet when needed
        val key = CoordKey(config.coordinates.groupId, config.coordinates.artifactId, Packaging("jar"), None)
        lookup.get(key)
          .map(node => collectTransitiveDeps(config, node, lookup))
          .map(node => addAliases(config, node))
          .map(node => addTags(config, node))
      }
      .map(node => new CoordKey(node.baseDependency.coordinates) -> node)
      .toSeq
  }

  private def update(in: Map[CoordKey, BazelDependencyNode],
                     withValues: Seq[(CoordKey, BazelDependencyNode)]): Map[CoordKey, BazelDependencyNode] = {
    in ++ withValues
  }

  private def addAliases(config: Dependency, dependencyNode: BazelDependencyNode): BazelDependencyNode = {
    val aliases = config.aliases
    if (aliases.nonEmpty)
      dependencyNode.copy(
        baseDependency = dependencyNode.baseDependency.withAliases(aliases)
      )
    else
      dependencyNode
  }

  private def addTags(config: Dependency, dependencyNode: BazelDependencyNode): BazelDependencyNode = {
    val tags = config.tags
    if (tags.nonEmpty)
      dependencyNode.copy(
        baseDependency = dependencyNode.baseDependency.withTags(tags)
      )
    else
      dependencyNode
  }

  private def collectTransitiveDeps(config: Dependency,
                                    dependencyNode: BazelDependencyNode,
                                    lookup: Map[CoordKey, BazelDependencyNode]): BazelDependencyNode = {
    if (config.flattenTransitiveDeps) {
      @scala.annotation.tailrec
      def accumulate(current: List[Dependency], collected: Set[Dependency]): Set[Dependency] = current match {
        case dep :: rest =>
          lookup.get(new CoordKey(dep.coordinates)) match {
            case None => accumulate(rest, collected + dep)
            case Some(node) => accumulate(node.dependencies.toList ++ rest, collected + dep)
          }
        case Nil =>
          collected
      }

      dependencyNode.copy(transitiveClosureDeps = accumulate(dependencyNode.dependencies.toList, Set()))
    } else {
      dependencyNode
    }
  }
}

private case class CoordKey(groupId: String, artifactId: String, packaging: Packaging, classifier: Option[String]) {
  def this(coordinates: Coordinates) = this(
    coordinates.groupId, coordinates.artifactId, coordinates.packaging, coordinates.classifier
  )
}

