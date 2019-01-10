package com.wix.build.sync.snapshot

import java.nio.file.Files

import better.files.File
import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver._
import com.wix.build.bazel.{FWThirdPartyPaths, FileSystemBazelLocalWorkspace}
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven.{Coordinates, _}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.{After, Scope}

//noinspection TypeAnnotation
class FWDependenciesSynchronizerCliE2E extends SpecWithJUnit {
  sequential

  "FW Dependencies synchronizer CLI" should {
    "sync new FW leaf version to core-server-build-tools" in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(dependencyA, dependencyB))

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString, "--fw-leaf-artifact", artifactA.serialized)
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependencies = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB)
    }

    "update transitive dep versions" in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(dependencyA, dependencyB.withVersion("another-version")))

      fwManagedDepsWorkspace.hasDependencies(DependencyNode(dependencyA, Set(dependencyB)))

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString, "--fw-leaf-artifact", artifactA.serialized)
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependencies = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB.withVersion("another-version"))
    }

    "filter out third_party deps" in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(dependencyA, dependencyB.withVersion("another-version")))

      thirdPartyManagedDepsWorkspace.hasDependencies(aRootDependencyNode(dependencyB))

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString, "--fw-leaf-artifact", artifactA.serialized)
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependencies = Set(artifactB))
      targetRepo must notIncludeImportExternalRulesInWorkspace(artifactB.withVersion("another-version"))
    }

    "filter out wix_framework_leaf" in new basicCtx {
      val fwLeaf = Coordinates("com.wix.common", "wix-framework-leaf", "1.0.0", Packaging("pom"))
      val fwLeafPomDep = asCompileDependency(fwLeaf)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(fwLeafPomDep, dependencyB))

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString, "--fw-leaf-artifact", fwLeaf.serialized)
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactB)
      targetRepo.bazelExternalDependencyFor(fwLeaf).libraryRule must beNone
    }

    "support syncing additional deps to managed deps repo " in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)
      val dependencyC = asCompileDependency(artifactC)
      val dependencyD = asCompileDependency(artifactD)

      givenAetherResolverForDependencies(SingleDependency(dependencyA, dependencyB), dependencyC, dependencyD)

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString,
        "--fw-leaf-artifact", artifactA.serialized, "--additional-deps", s"${artifactC.serialized},${artifactD.serialized}")
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependencies = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB)
      targetRepo must includeImportExternalTargetWith(artifactC)
      targetRepo must includeImportExternalTargetWith(artifactD)
    }
  }

  trait basicCtx extends Scope with After {
    val remoteMavenRepo = new FakeMavenRepository()

    override def after: Any = remoteMavenRepo.stop()

    val artifactA = Coordinates("com.aaa", "A-direct", "1.0.0")
    val artifactB = Coordinates("com.bbb", "B-direct", "2.0.0")
    val artifactC = Coordinates("com.ccc", "C-direct", "3.0.0")
    val artifactD = Coordinates("com.ccc", "D-direct", "3.0.0")

    val managedDepsWorkspaceRepo = Files.createTempDirectory("managed-deps")
    val managedDepsRepoPath = File(managedDepsWorkspaceRepo)
    val fwManagedDepsWorkspace = new FileSystemBazelLocalWorkspace(managedDepsRepoPath, FWThirdPartyPaths())
    val thirdPartyManagedDepsWorkspace = new FileSystemBazelLocalWorkspace(managedDepsRepoPath)

    val modulePath = Files.createTempDirectory("local-module")

    val targetRepo = new BazelWorkspaceDriver(fwManagedDepsWorkspace)

    def givenAetherResolverForDependency(node: SingleDependency) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(node.dependant.coordinates, node.dependency)
      val dependencyDescriptor = ArtifactDescriptor.rootFor(node.dependency.coordinates)

      remoteMavenRepo.addArtifacts(Set(dependantDescriptor,dependencyDescriptor))
      remoteMavenRepo.addCoordinates(Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT"))
      remoteMavenRepo.start()
    }

    def givenAetherResolverForDependencies(node: SingleDependency, dependency: Dependency*) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(node.dependant.coordinates, node.dependency)
      val dependencyDescriptor = ArtifactDescriptor.rootFor(node.dependency.coordinates)

      remoteMavenRepo.addArtifacts(Set(dependantDescriptor,dependencyDescriptor) ++ dependency.map(d => ArtifactDescriptor.rootFor(d.coordinates)).toSet)
      remoteMavenRepo.addCoordinates(Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT"))
      remoteMavenRepo.start()
    }
  }
}