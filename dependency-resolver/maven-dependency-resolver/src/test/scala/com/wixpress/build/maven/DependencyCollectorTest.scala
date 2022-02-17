package com.wix.build.maven

import com.wix.build.maven._
import com.wix.build.maven.MavenMakers.aDependency
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class DependencyCollectorTest extends SpecificationWithJUnit {

  "DependencyCollector" >> {
    "when no new dependencies were added after initialization" should {
      "return empty dependency set" in {
        val collector = new DependencyCollector()
        collector.dependencySet() mustEqual Set.empty[Dependency]
      }

      "return a set with dependencies after they were added using the addOrOverrideDependencies call" in {
        val collector = new DependencyCollector()
        val newDependencies = Set(aDependency("a"))

        collector
          .addOrOverrideDependencies(newDependencies)
          .dependencySet() must contain(allOf(newDependencies))
      }


      "merge all exclusions for each dependency" in {
        val otherDependency = aDependency("guava", exclusions = Set(MavenMakers.anExclusion("a")))
        val newDependencies = Set(
          aDependency("b", exclusions = Set(MavenMakers.anExclusion("a"))),
          aDependency("b", exclusions = Set(MavenMakers.anExclusion("c"))),
          aDependency("b", exclusions = Set(MavenMakers.anExclusion("d"))),
          otherDependency)
        val collector = new DependencyCollector(newDependencies)

        collector.mergeExclusionsOfSameCoordinates().dependencySet() mustEqual Set(
          aDependency("b", exclusions = Set(
            MavenMakers.anExclusion("a"),
            MavenMakers.anExclusion("c"),
            MavenMakers.anExclusion("d"))),
          otherDependency)
      }
    }

    "after already collect dependency A," should {
      trait oneCollectedDependencyCtx extends Scope {
        val existingDependency: Dependency = aDependency("existing")

        def collector = new DependencyCollector(Set(existingDependency))
      }

      "return a set with both A and new dependencies after they were added using the with dependencies call" in new oneCollectedDependencyCtx {
        val newDependency = aDependency("new")
        collector
          .addOrOverrideDependencies(Set(newDependency))
          .dependencySet() mustEqual Set(existingDependency, newDependency)
      }

      "allow overriding the version of A by adding a set with a different version of A" in new oneCollectedDependencyCtx {
        val newDependency = existingDependency.withVersion("different-version")

        collector
          .addOrOverrideDependencies(Set(newDependency))
          .dependencySet() mustEqual Set(newDependency)
      }

    }

  }
}
