package com.wix.build.sync.fw

import java.nio.file.Files

import better.files.File
import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver._
import com.wix.build.bazel.FileSystemBazelLocalWorkspace
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven.{Coordinates, _}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.{After, Scope}

//noinspection TypeAnnotation
class FWDependenciesToSingleRepoSynchronizerCliE2E extends SpecWithJUnit {
  sequential

  "FW Dependencies synchronizer CLI" should {
    "sync new FW leaf version to some repo" in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(dependencyA, dependencyB))

      val args = Array("--binary-repo", remoteMavenRepo.url,"--target_repo", targetRepoPath.toString,
        "--managed_deps_repo", managedDepsRepoPath.toString, "--fw-leaf-artifact", artifactA.serialized)
      FWDependenciesToSingleRepoSynchronizerCli.main(args)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependencies = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB)
    }
  }

  trait basicCtx extends Scope with After {
    val remoteMavenRepo = new FakeMavenRepository()

    override def after: Any = remoteMavenRepo.stop()

    val artifactA = Coordinates("com.aaa", "A-direct", "1.0.0")
    val artifactB = Coordinates("com.bbb", "B-direct", "2.0.0")

    val targetRepoPath = File(Files.createTempDirectory("target-repo"))
    val targetRepoWorkspace = new FileSystemBazelLocalWorkspace(targetRepoPath)

    val managedDepsRepoPath = File(Files.createTempDirectory("managed-deps"))
    val thirdPartyManagedDepsWorkspace = new FileSystemBazelLocalWorkspace(managedDepsRepoPath)

    val targetRepo = new BazelWorkspaceDriver(targetRepoWorkspace)

    def givenAetherResolverForDependency(node: SingleDependency) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(node.dependant.coordinates, node.dependency)
      val dependencyDescriptor = ArtifactDescriptor.rootFor(node.dependency.coordinates)

      remoteMavenRepo.addArtifacts(Set(dependantDescriptor,dependencyDescriptor))
      remoteMavenRepo.addCoordinates(Coordinates.deserialize("com.wix.common:third-party-dependencies:pom:100.0.0-SNAPSHOT"))
      remoteMavenRepo.start()
    }
  }
}