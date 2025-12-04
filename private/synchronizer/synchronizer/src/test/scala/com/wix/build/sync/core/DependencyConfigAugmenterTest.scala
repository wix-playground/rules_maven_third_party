package com.wix.build.sync.core

import com.wix.build.maven.{Coordinates, _}
import org.specs2.mutable.SpecWithJUnit

class DependencyConfigAugmenterTest extends SpecWithJUnit {
  "collects transitive deps" in {
    val node1 = BazelDependencyNode(baseDependency = aDep("dep1"), Set(aDep("dep2"), aDep("dep3")))
    val node2 = BazelDependencyNode(baseDependency = aDep("dep2"), Set(aDep("dep3")))
    val node3 = BazelDependencyNode(baseDependency = aDep("dep3"), Set(aDep("dep4"), aDep("dep5")))
    val deps = Set(node1, node2, node3)
    val dependenciesConfig = List(aDep(artifactId = "dep2", flattenTransitiveDeps = true))

    val node2withTransitiveDeps = BazelDependencyNode(
      baseDependency = aDep("dep2"),
      dependencies = Set(aDep("dep3")),
      transitiveClosureDeps = Set(
        aDep("dep3"),
        aDep("dep4"),
        aDep("dep5")
      ),
    )

    DependencyConfigAugmenter.augment(deps, dependenciesConfig) mustEqual Set(node1, node2withTransitiveDeps, node3)
  }

  "add aliases" in {
    val aliases = Set("alias1", "alias2")
    val node1 = BazelDependencyNode(baseDependency = aDep("dep1"), Set())
    val node2 = BazelDependencyNode(baseDependency = aDep("dep2"), Set())
    val node1WithAliases = BazelDependencyNode(baseDependency = aDep("dep1", aliases = aliases), Set())
    val deps = Set(node1, node2)

    val dependenciesConfig = List(aDep(artifactId = "dep1", aliases = aliases))

    DependencyConfigAugmenter.augment(deps, dependenciesConfig) mustEqual Set(node1WithAliases, node2)
  }

  "add tags" in {
    val tags = Set("alias1", "alias2")
    val node1 = BazelDependencyNode(baseDependency = aDep("dep1"), Set())
    val node2 = BazelDependencyNode(baseDependency = aDep("dep2"), Set())
    val node1WithTags = BazelDependencyNode(baseDependency = aDep("dep1", tags = tags), Set())
    val deps = Set(node1, node2)

    val dependenciesConfig = List(aDep(artifactId = "dep1", tags = tags))

    DependencyConfigAugmenter.augment(deps, dependenciesConfig) mustEqual Set(node1WithTags, node2)
  }

  "add testOnly" in {
    val node1 = BazelDependencyNode(baseDependency = aDep("dep1"), Set())
    val node2 = BazelDependencyNode(baseDependency = aDep("dep2"), Set())
    val node1WithTestOnly = BazelDependencyNode(baseDependency = aDep("dep1", isTestOnly = true), Set())
    val deps = Set(node1, node2)

    val dependenciesConfig = List(aDep(artifactId = "dep1", isTestOnly = true))

    DependencyConfigAugmenter.augment(deps, dependenciesConfig) mustEqual Set(node1WithTestOnly, node2)
  }

  def aDep(artifactId: String,
           groupId: String = "group",
           version: String = "version",
           scope: MavenScope = MavenScope.Compile,
           aliases: Set[String] = Set.empty,
           tags: Set[String] = Set.empty,
           flattenTransitiveDeps: Boolean = false,
           isTestOnly: Boolean = false): Dependency = Dependency(
    coordinates = Coordinates(groupId = groupId, artifactId = artifactId, version = version),
    scope = scope,
    aliases = aliases,
    tags = tags,
    flattenTransitiveDeps = flattenTransitiveDeps,
    isTestOnly = isTestOnly
  )
}
