package com.wix.build.sync.snapshot

import java.nio.file.Files

import better.files.File
import com.wix.build.sync.snapshot.SnapshotsToSingleRepoSynchronizerCliConfig.{BinariesRepoFlag, ManagedDepsRepoFlag, SnapshotModuleToSyncCoordinatesFlag, TargetRepoFlag}
import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver._
import com.wix.build.bazel.FileSystemBazelLocalWorkspace
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven.{Coordinates, _}
import com.wix.build.sync.e2e.FakePinnedDepsFileWriter
import org.specs2.mutable.{BeforeAfter, SpecWithJUnit}
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class SnapshotsToSingleRepoSynchronizerCliE2E extends SpecWithJUnit {
  sequential

  "Snapshot Dependencies synchronizer CLI" should {
    "sync dep A with transitive dep B to some repo" in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(dependencyA, dependencyB))

      runSnapshotsToSingleRepoSynchronizerCliFor(artifactA.serialized)

      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependenciesIgnoringVersion = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactB)
    }

    "sync multiple deps to some repo" in new basicCtx {
      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForCoords(artifactA, artifactB)
      runSnapshotsToSingleRepoSynchronizerCliFor(s"${artifactA.serialized},${artifactB.serialized}")

      targetRepo must includeImportExternalTargetWith(artifactA)
      targetRepo must includeImportExternalTargetWith(artifactB)
    }

    "sync dep A and give precedence to pinned transitive dep B" in new basicCtx {
      pinBtoLowerVersion()

      val dependencyA = asCompileDependency(artifactA)
      val dependencyB = asCompileDependency(artifactB)

      givenAetherResolverForDependency(SingleDependency(dependencyA, dependencyB))
      val artifactBButWithPinnedVersion = artifactB.withVersion(PinnedBLowerVersion)
      givenAetherResolverForCoords(artifactBButWithPinnedVersion)

      runSnapshotsToSingleRepoSynchronizerCliFor(artifactA.serialized)

      //remember, the compileTimeDependencies don't care about version when comparing!
      targetRepo must includeImportExternalTargetWith(artifactA, compileTimeDependenciesIgnoringVersion = Set(artifactB))
      targetRepo must includeImportExternalTargetWith(artifactBButWithPinnedVersion)
    }

    "give pinned dep precedence over direct managed dep" in new basicCtx {
      pinBtoLowerVersion()

      val artifactBButWithPinnedVersion = artifactB.withVersion(PinnedBLowerVersion)

      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactB)))

      val unrelatedArtifact = Coordinates("com.blah", "blah", "1.0.0")

      givenAetherResolverForCoords(artifactBButWithPinnedVersion, unrelatedArtifact)
      runSnapshotsToSingleRepoSynchronizerCliFor(unrelatedArtifact.serialized)

      targetRepo must includeImportExternalTargetWith(artifactBButWithPinnedVersion)
    }

    "give pinned dep precedence over requested snapshotToSync" in new basicCtx {
      pinBtoLowerVersion()

      val artifactBButWithPinnedVersion = artifactB.withVersion(PinnedBLowerVersion)

      givenAetherResolverForCoords(artifactBButWithPinnedVersion)
      runSnapshotsToSingleRepoSynchronizerCliFor(artifactB.serialized)

      targetRepo must includeImportExternalTargetWith(artifactBButWithPinnedVersion)
    }

    "delete local dep with requested version, when requested is equal to managed version" in new basicCtx {
      val artifactBButWithOtherLocalVersion = artifactB.withVersion("0.5")
      targetRepoWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactBButWithOtherLocalVersion)))

      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactB)))
      givenAetherResolverForCoords(artifactB)
      runSnapshotsToSingleRepoSynchronizerCliFor(artifactB.serialized)

      targetRepo must notIncludeImportExternalRulesInWorkspace(artifactB)
    }

    "delete all local deps with versions equal to managed versions" in new basicCtx {
      targetRepoWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactB)))
      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactB)))

      val unrelatedArtifact = Coordinates("com.blah", "booya", "1.0.0")
      givenAetherResolverForCoords(unrelatedArtifact)
      runSnapshotsToSingleRepoSynchronizerCliFor(unrelatedArtifact.serialized)

      targetRepo must notIncludeImportExternalRulesInWorkspace(artifactB)
    }

    "don't delete local dep if equal to managed version and managed does not have neverlink" in new basicCtx {
      targetRepoWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA).withIsNeverLink(true), checksum = None, srcChecksum = None))
      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

      val unrelatedArtifact = Coordinates("com.blah", "booya", "1.0.0")
      givenAetherResolverForCoords(unrelatedArtifact)
      runSnapshotsToSingleRepoSynchronizerCliFor(unrelatedArtifact.serialized)

      targetRepo must includeImportExternalTargetWith(artifactA, neverlink = true)
    }

    "delete local dep with neverlink if equal to managed version and managed has neverlink" in new basicCtx {
      targetRepoWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA).withIsNeverLink(true)))
      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA).withIsNeverLink(true)))

      val unrelatedArtifact = Coordinates("com.blah", "booya", "1.0.0")
      givenAetherResolverForCoords(unrelatedArtifact)
      runSnapshotsToSingleRepoSynchronizerCliFor(unrelatedArtifact.serialized)

      targetRepo must notIncludeImportExternalRulesInWorkspace(artifactA)
    }

    "delete local dep without neverlink if equal to managed version and managed has neverlink" in new basicCtx {
      targetRepoWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))
      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA).withIsNeverLink(true)))

      val unrelatedArtifact = Coordinates("com.blah", "booya", "1.0.0")
      givenAetherResolverForCoords(unrelatedArtifact)
      runSnapshotsToSingleRepoSynchronizerCliFor(unrelatedArtifact.serialized)

      targetRepo must notIncludeImportExternalRulesInWorkspace(artifactA)
    }

    "don't add local dep if requested is equal to managed version and managed has neverlink" in new basicCtx {
      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA).withIsNeverLink(true)))

      givenAetherResolverForCoords(artifactA)
      runSnapshotsToSingleRepoSynchronizerCliFor(artifactA.serialized)

      targetRepo must notIncludeImportExternalRulesInWorkspace(artifactA)
    }

    "don't add local dep if requested is equal to managed version" in new basicCtx {
      thirdPartyManagedDepsWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

      givenAetherResolverForCoords(artifactA)
      runSnapshotsToSingleRepoSynchronizerCliFor(artifactA.serialized)

      targetRepo must notIncludeImportExternalRulesInWorkspace(artifactA)
    }

  }

  trait basicCtx extends Scope with BeforeAfter {
    val PinnedBLowerVersion = "0.7"

    val remoteMavenRepo = new FakeMavenRepository()

    override def after: Any = remoteMavenRepo.stop()

    override def before: Unit = {
      remoteMavenRepo.start()
    }

    val artifactA = Coordinates("com.aaa", "A-direct", "1.0.0")
    val artifactB = Coordinates("com.bbb", "B-direct", "1.0.0")

    val targetRepoPath = File(Files.createTempDirectory("target-repo"))
    val targetRepoWorkspace = new FileSystemBazelLocalWorkspace(targetRepoPath)

    val managedDepsRepoPath = File(Files.createTempDirectory("managed-deps"))
    val thirdPartyManagedDepsWorkspace = new FileSystemBazelLocalWorkspace(managedDepsRepoPath)

    val targetRepo = new BazelWorkspaceDriver(targetRepoWorkspace)

    private def argsWithSnapshotsToSync(snapshotModuleToSync: String) = Array(
      s"--$BinariesRepoFlag", remoteMavenRepo.url,
      s"--$TargetRepoFlag", targetRepoPath.toString,
      s"--$ManagedDepsRepoFlag", managedDepsRepoPath.toString,
      s"--$SnapshotModuleToSyncCoordinatesFlag", snapshotModuleToSync
    )

    def pinBtoLowerVersion() = {
      new FakePinnedDepsFileWriter(targetRepoPath.path).write(PinnedBLowerVersion)
    }

    def runSnapshotsToSingleRepoSynchronizerCliFor(snapshotModuleToSync: String): Unit = {
      SnapshotsToSingleRepoSynchronizerCli.main(argsWithSnapshotsToSync(snapshotModuleToSync))
    }

    def givenAetherResolverForDependency(node: SingleDependency) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(node.dependant.coordinates, node.dependency)
      val dependencyDescriptor = ArtifactDescriptor.rootFor(node.dependency.coordinates)

      remoteMavenRepo.addArtifacts(Set(dependantDescriptor,dependencyDescriptor))
    }

    def givenAetherResolverForCoords(coords: Coordinates*) = {
      coords.foreach{
        d =>  val dependencyDescriptor = ArtifactDescriptor.rootFor(d)
          remoteMavenRepo.addSingleArtifact(dependencyDescriptor)
      }
    }
  }
}