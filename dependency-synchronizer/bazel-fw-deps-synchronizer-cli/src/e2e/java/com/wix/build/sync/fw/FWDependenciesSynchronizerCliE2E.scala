package com.wix.build.sync.fw

import java.nio.file.Files

import better.files.File
import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver.includeImportExternalTargetWith
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

      private val newVersion = "1.0.0-SNAPSHOT"

      givenAetherResolverForDependency(SingleDependency(dependencyA.withVersion(newVersion), dependencyB))

      val args = Array("--binary-repo", remoteMavenRepo.url,"--managed_deps_repo", managedDepsRepoPath.toString,  "--fw_dep", artifactA.serialized, "--version", newVersion)
      FWDependenciesSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA.withVersion(newVersion), compileTimeDependencies = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB)
    }
  }

  trait basicCtx extends Scope with After {
    val remoteMavenRepo = new FakeMavenRepository()

    override def after: Any = remoteMavenRepo.stop()

    val artifactA = Coordinates("com.aaa", "A-direct", "1.0.0")
    val artifactB = Coordinates("com.bbb", "B-direct", "2.0.0")

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
  }
}