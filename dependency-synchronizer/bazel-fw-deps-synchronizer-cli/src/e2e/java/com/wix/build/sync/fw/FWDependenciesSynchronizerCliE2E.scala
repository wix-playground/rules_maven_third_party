package com.wix.build.sync.fw

import java.nio.file.Files

import better.files.File
import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver.{BazelWorkspaceDriverExtensions, includeImportExternalTargetWith}
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

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString, artifactA.serialized)
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependencies = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB)
    }

    "update transitive dep versions" in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(dependencyA, dependencyB.withVersion("another-version")))

      managedDepsWorkspace.hasDependencies(DependencyNode(dependencyA, Set(dependencyB)))

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString, artifactA.serialized)
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependencies = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB.withVersion("another-version"))
    }
  }

  trait basicCtx extends Scope with After {
    val remoteMavenRepo = new FakeMavenRepository()

    override def after: Any = remoteMavenRepo.stop()

    val artifactA = Coordinates("com.aaa", "A-direct", "1.0.0")
    val artifactB = Coordinates("com.bbb", "B-direct", "2.0.0")
    val artifactC = Coordinates("com.ccc", "C-direct", "3.0.0")

    val managedDepsWorkspaceRepo = Files.createTempDirectory("managed-deps")
    val managedDepsRepoPath = File(managedDepsWorkspaceRepo)
    val managedDepsWorkspace = new FileSystemBazelLocalWorkspace(managedDepsRepoPath, FWThirdPartyPaths())

    val modulePath = Files.createTempDirectory("local-module")

    val targetRepo = new BazelWorkspaceDriver(managedDepsWorkspace)

    def givenAetherResolverForDependency(node: SingleDependency) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(node.dependant.coordinates, node.dependency)
      val dependencyDescriptor = ArtifactDescriptor.rootFor(node.dependency.coordinates)

      remoteMavenRepo.addArtifacts(Set(dependantDescriptor,dependencyDescriptor))
      remoteMavenRepo.addCoordinates(Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT"))
      remoteMavenRepo.start()
    }

    def givenAetherResolverForDependency(node: SingleTransitiveDependency) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(node.dependant.coordinates, node.dependency)
      val dependencyDescriptor = ArtifactDescriptor.rootFor(node.dependency.coordinates)

      remoteMavenRepo.addArtifacts(Set(dependantDescriptor,dependencyDescriptor))
      remoteMavenRepo.addCoordinates(Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT"))
      remoteMavenRepo.start()
    }
  }
}