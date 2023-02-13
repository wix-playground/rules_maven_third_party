package com.wix.build.sync

import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver._
import com.wix.build.bazel._
import com.wix.build.maven.FakeMavenDependencyResolver._
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class UserAddedDepsDiffSynchronizerTest extends SpecWithJUnit {
  val thirdPartyPath = "third_party"

  "UserAddedDepsDiffSynchronizer" should {
    "when persisting changes" should {
      "add third party dependencies to repo" in new ctx {
        val newArtifacts = Set(artifactA, artifactB)

        synchronizer.syncThirdParties(newArtifacts.map(toDependency))

        newArtifacts.map { c => targetRepoDriver.bazelExternalDependencyFor(c).importExternalRule } must contain(beSome[ImportExternalRule]).forall
      }

      "only add unmanaged dependencies to local repo" in new ctx {
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

        synchronizer.syncThirdParties(Set(artifactA, artifactB).map(toDependency))

        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beNone
        targetRepoDriver.bazelExternalDependencyFor(artifactB).importExternalRule must beSome[ImportExternalRule]
      }

      "only add unmanaged dependencies to local repo - neverlink in managed wins" in new ctx {
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA).withIsNeverLink(true)))

        synchronizer.syncThirdParties(Set(artifactA, artifactB).map(toDependency))

        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beNone
        targetRepoDriver.bazelExternalDependencyFor(artifactB).importExternalRule must beSome[ImportExternalRule]
      }

      "don't add local dep if managed has it and also maven central has other opinion with exclusions" in new ctx {
        managedDepsLocalWorkspace.hasDependencies(BazelDependencyNode(asCompileDependency(artifactA), Set(asCompileDependency(artifactB))), aRootBazelDependencyNode(asCompileDependency(artifactB)))

        val transitiveExcluded = Coordinates("com.tran", "C-tran", "2.0.0")
        override val resolver = givenFakeResolverForDependencies(singleDependencies =
          Set(SingleDependency(toDependency(artifactA), toDependency(artifactB.copy(version = "0.1")).withExclusions(Set(Exclusion(transitiveExcluded))))))

        override val userAddedDepsDiffCalculator = new UserAddedDepsDiffCalculator(targetFakeBazelRepository, managedDepsFakeBazelRepository,
          resolver, false, _ => None, Set[Coordinates](), NeverLinkResolver())

        override def synchronizer = new UserAddedDepsDiffSynchronizer(userAddedDepsDiffCalculator, writerFor())

        synchronizer.syncThirdParties(Set(toDependency(artifactA)))
        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beNone
      }

      "don't add local dep if managed has it with exclusions" in new ctx {
        managedDepsLocalWorkspace.hasDependencies(BazelDependencyNode(asCompileDependency(artifactA).withExclusions(Set(Exclusion(artifactB))), Set()))

        synchronizer.syncThirdParties(Set(toDependency(artifactA))).updatedBazelLocalNodes must beEmpty
      }

      "remove local dep if managed has it with exclusions" in new ctx {
        targetFakeLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA.withVersion("0.3"))))
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA).withExclusions(Set(Exclusion(artifactB)))))

        synchronizer.syncThirdParties(Set(artifactA).map(toDependency))

        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beNone
      }

      "remove local dep if managed has it, even when versions of transitive deps differ" in new ctx {
        managedDepsLocalWorkspace.hasDependencies(BazelDependencyNode(asCompileDependency(artifactA), Set(asCompileDependency(artifactB))), aRootBazelDependencyNode(asCompileDependency(artifactB)))
        val artifactBWithOtherVersion: Dependency = asCompileDependency(artifactB).withVersion("0.3")
        targetFakeLocalWorkspace.hasDependencies(BazelDependencyNode(asCompileDependency(artifactA), Set(artifactBWithOtherVersion)), aRootBazelDependencyNode(artifactBWithOtherVersion, checksum = None, srcChecksum = None))

        synchronizer.syncThirdParties(Set(toDependency(artifactA)))

        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beNone
        targetRepoDriver must includeImportExternalTargetWith(artifactBWithOtherVersion.coordinates, thirdPartyPath = thirdPartyPath)
      }

      "remove local dep if requested is equal to managed version" in new ctx {
        targetFakeLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA.withVersion("0.3"))))
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

        synchronizer.syncThirdParties(Set(artifactA).map(toDependency))

        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beNone
      }

      "don't remove local dep if requested has neverlink and managed does not" in new ctx {
        targetFakeLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA.withVersion("0.3")).withIsNeverLink(true)))
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

        synchronizer.syncThirdParties(Set(artifactA).map(toDependency))

        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beSome[ImportExternalRule]
      }

      "update local dep if requested is NOT identical to managed" in new ctx {
        targetFakeLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA.withVersion("0.3"))))
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

        val updatedArtifactButStillLowerThanManaged = artifactA.withVersion("0.4")
        synchronizer.syncThirdParties(Set(updatedArtifactButStillLowerThanManaged).map(toDependency))

        targetRepoDriver must includeImportExternalTargetWith(updatedArtifactButStillLowerThanManaged, thirdPartyPath = thirdPartyPath)
      }

      "remove all local deps with versions equal to managed versions" in new ctx {
        targetFakeLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

        val unrelatedArtifact = Coordinates("com.blah", "booya", "1.0.0")
        synchronizer.syncThirdParties(Set(unrelatedArtifact).map(toDependency))

        targetRepoDriver.bazelExternalDependencyFor(artifactA).importExternalRule must beNone
      }

      "update dependency's version in local repo" in new ctx {
        targetFakeLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactA)))

        val updatedArtifact = artifactA.copy(version = "2.0.0")
        synchronizer.syncThirdParties(Set(updatedArtifact).map(toDependency))

        targetRepoDriver must includeImportExternalTargetWith(updatedArtifact, thirdPartyPath = thirdPartyPath)
      }

      "add linkable suffix to relevant transitive dep" in new linkableCtx {
        val diffSynchronizer: UserAddedDepsDiffSynchronizer = synchronizerWithLinkableArtifact(artifactB)
        targetFakeLocalWorkspace.hasDependencies(BazelDependencyNode(asCompileDependency(artifactA), Set(asCompileDependency(artifactB))))
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactB)))

        diffSynchronizer.syncThirdParties(Set(artifactA).map(toDependency))

        targetRepoDriver.transitiveCompileTimeDepOf(artifactA) must contain(contain("//:linkable"))

      }

      "if diffCalculator contains closure error, don't persist, print the closure error and exit with error" in new ctx {
        val spyDiffWriter = new SpyDiffWriter()
        val blah = new UserAddedDepsDiffSynchronizer(new AlwaysFailsDiffCalculator(), spyDiffWriter)
        blah.syncThirdParties(Set()) must throwA[IllegalArgumentException]

        spyDiffWriter.timesCalled must_== (0)
      }
    }

    "when calculating diff" should {

      //TODO - these 2 tests to be extracted to a new UserAddedDepsDiffCalculatorTest
      "calculate difference from managed" in new ctx {
        val newArtifacts = Set(artifactA, artifactB)

        val nodes: Set[BazelDependencyNode] = newArtifacts.map(a => aRootBazelDependencyNode(asCompileDependency(a), checksum = None, srcChecksum = None))
        userAddedDepsDiffCalculator.resolveUpdatedLocalNodes(newArtifacts.map(toDependency)) mustEqual DiffResult(nodes, Set(), Set())
      }

      "resolve local deps closure when a local transitive dependency is only found in managed set" in new ctx {
        targetFakeLocalWorkspace.hasDependencies(BazelDependencyNode(asCompileDependency(artifactA), Set(asCompileDependency(artifactB))))
        managedDepsLocalWorkspace.hasDependencies(aRootBazelDependencyNode(asCompileDependency(artifactB)))

        userAddedDepsDiffCalculator.resolveUpdatedLocalNodes(Set()).preExistingLocalNodes must contain(DependencyNode(asCompileDependency(artifactA), Set(asCompileDependency(artifactB))))
      }
    }
  }

  trait ctx extends Scope {
    val targetFakeLocalWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = "some_local_workspace_name")
    val targetFakeBazelRepository = new InMemoryBazelRepository(targetFakeLocalWorkspace)

    val managedDepsWorkspaceName = "some_external_workspace_name"
    val managedDepsLocalWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = managedDepsWorkspaceName)
    val managedDepsFakeBazelRepository = new InMemoryBazelRepository(managedDepsLocalWorkspace)
    val importExternalLoadStatement = ImportExternalLoadStatement(importExternalRulePath = "@some_workspace//:import_external.bzl", importExternalMacroName = "some_import_external")

    val dependencyManagementCoordinates = Coordinates("some.group", "deps-management", "1.0", Packaging("pom"))

    val artifactA = Coordinates("com.aaa", "A-direct", "1.0.0")
    val artifactB = Coordinates("com.bbb", "B-direct", "2.0.0")

    def toDependency(coordinates: Coordinates): Dependency = {
      // scope here is of no importance as it is used on third_party and workspace only
      Dependency(coordinates, MavenScope.Compile)
    }

    val targetRepoDriver = new BazelWorkspaceDriver(targetFakeLocalWorkspace)

    val resolver = givenFakeResolverForDependencies(rootDependencies = Set(asCompileDependency(dependencyManagementCoordinates)))
    val userAddedDepsDiffCalculator = new UserAddedDepsDiffCalculator(targetFakeBazelRepository, managedDepsFakeBazelRepository,
      resolver, false, _ => None, Set[Coordinates](), NeverLinkResolver())

    def synchronizer = new UserAddedDepsDiffSynchronizer(userAddedDepsDiffCalculator,
      new DefaultDiffWriter(
        targetFakeBazelRepository,
        maybeManagedDepsRepoPath = None,
        NeverLinkResolver(),
        importExternalLoadStatement,
        maybeGitAdder = None,
        isManagedInvocation = false
      )
    )

    def writerFor() = {
      new DefaultDiffWriter(targetFakeBazelRepository,
        maybeManagedDepsRepoPath = None,
        NeverLinkResolver(),
        importExternalLoadStatement,
        maybeGitAdder = None,
        isManagedInvocation = false
      )
    }
  }

  trait linkableCtx extends ctx {
    def synchronizerWithLinkableArtifact(artifact: Coordinates) = new UserAddedDepsDiffSynchronizer(
      new UserAddedDepsDiffCalculator(
        targetFakeBazelRepository,
        managedDepsFakeBazelRepository,
        resolver,
        false,
        _ => None,
        Set[Coordinates](),
        NeverLinkResolver()
      ),
      new DefaultDiffWriter(
        targetFakeBazelRepository,
        maybeManagedDepsRepoPath = None,
        NeverLinkResolver(overrideGlobalNeverLinkDependencies = Set(artifact)),
        importExternalLoadStatement,
        maybeGitAdder = None,
        isManagedInvocation = false
      )
    )
  }

  class AlwaysFailsDiffCalculator extends DiffCalculatorAndAggregator {
    override def resolveUpdatedLocalNodes(userAddedDependencies: Set[Dependency], artifactIdToDebug: Option[String] = None): DiffResult = {
      val dependencyOfTheRootNode = aDependency("otherArtifactId")

      val updatedLocalNodes = Set(BazelDependencyNode(asCompileDependency(someCoordinates("someArtifactId")), dependencies = Set(dependencyOfTheRootNode)))
      val diffResultWithNonFullClosure = DiffResult(updatedLocalNodes, preExistingLocalNodes = Set(), managedNodes = Set())
      diffResultWithNonFullClosure
    }
  }

  class SpyDiffWriter extends DiffWriter {
    var timesCalled = 0

    override def persistResolvedDependencies(userAddedDependecies: Set[Dependency],
                                             divergentLocalDependencies: Set[BazelDependencyNode],
                                             libraryRulesNodes: Set[DependencyNode],
                                             localDepsToDelete: Set[DependencyNode]): Unit =
      timesCalled += 1
  }

}