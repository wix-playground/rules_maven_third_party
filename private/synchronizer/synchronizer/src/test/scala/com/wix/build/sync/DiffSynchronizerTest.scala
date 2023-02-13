package com.wix.build.sync

import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver.{includeImportExternalTargetWith, _}
import com.wix.build.bazel._
import com.wix.build.git.GitAdder
import com.wix.build.maven.FakeMavenDependencyResolver._
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven._
import com.wix.build.sync.DependenciesRemoteStorageTestSupport.remoteStorageWillReturn
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class DiffSynchronizerTest extends SpecificationWithJUnit {
  val thirdPartyPath = "third_party"

  "Diff Synchronizer" should {
    "not persist jar import because has same coordinates in managed list" in new baseCtx {
      val aManagedDependency = aDependency("existing")

      givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(aManagedDependency))

      val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), localNodes = Set(aRootDependencyNode(aManagedDependency)))

      localWorkspace must notIncludeImportExternalRulesInWorkspace(aManagedDependency.coordinates)
    }

    "persist jar import with local divergent version" in new baseCtx {
      givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(managedDependency))

      val divergentDependency = managedDependency.withVersion("new-version")

      val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
      val synchronizer = givenSynchornizerFor(resolver, gitAdder = Some(gitAdder))

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentDependency)))

      localWorkspace must includeImportExternalTargetWith(divergentDependency.coordinates, thirdPartyPath = thirdPartyPath)
      gitAdder.addedFiles must be_===(Set("third_party/some_group.bzl", "third_party.bzl"))
    }

    "persist jar import with local provided scope " in new baseCtx {
      givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(managedDependency))

      val divergentDependency = managedDependency.withScope(MavenScope.Provided)

      val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentDependency)))

      localWorkspace must includeImportExternalTargetWith(divergentDependency.coordinates, neverlink = true, thirdPartyPath = thirdPartyPath)
    }

    "persist dependency with its dependencies (managed has no dependency)" in new baseCtx {
      givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(managedDependency))

      val divergentDependency = managedDependency.withVersion("new-version")
      val singleDependency = SingleDependency(divergentDependency, transitiveDependency)

      val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), dependencyNodesFrom(singleDependency))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentDependency.coordinates,
        runtimeDependencies = Set(transitiveDependency.coordinates), thirdPartyPath = thirdPartyPath)

      localWorkspace must includeImportExternalTargetWith(
        artifact = transitiveDependency.coordinates,
        thirdPartyPath = thirdPartyPath
      )
    }

    "persist dependency's jar import without its dependency because managed has the dependency as well" in new baseCtx {
      val originalSingleDependency = SingleDependency(managedDependency, transitiveDependency)

      givenBazelWorkspaceWithManagedDependencies(dependencyNodesFrom(originalSingleDependency))

      val divergentDependency = managedDependency.withVersion("new-version")
      val divergentSingleDependency = SingleDependency(divergentDependency, transitiveDependency)

      val resolver = givenFakeResolverForDependencies(Set(originalSingleDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), dependencyNodesFrom(divergentSingleDependency))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentDependency.coordinates,
        runtimeDependencies = Set(transitiveDependency.coordinates),
        thirdPartyPath = thirdPartyPath
      )

      localWorkspace must notIncludeImportExternalRulesInWorkspace(transitiveDependency.coordinates)
    }

    "persist jar import with local divergent version and divergent dependency" in new baseCtx {
      val originalSingleDependency = SingleDependency(managedDependency, transitiveDependency)

      val divergentTransitiveDependency = aDependency("local-transitive")

      givenBazelWorkspaceWithManagedDependencies(dependencyNodesFrom(originalSingleDependency))

      val divergentDependency = managedDependency.withVersion("new-version")
      val divergentSingleDependency = SingleDependency(divergentDependency, divergentTransitiveDependency)

      val resolver = givenFakeResolverForDependencies(Set(originalSingleDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), dependencyNodesFrom(divergentSingleDependency))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentDependency.coordinates,
        compileTimeDependenciesIgnoringVersion = Set(divergentTransitiveDependency.coordinates), thirdPartyPath = thirdPartyPath)

      localWorkspace must includeImportExternalTargetWith(divergentTransitiveDependency.coordinates, thirdPartyPath = thirdPartyPath)
    }

    "persist dependency with divergent dependency" in new baseCtx {
      val originalSingleDependency = SingleDependency(managedDependency, transitiveDependency)

      val divergentTransitiveDependency = aDependency("local-transitive")

      givenBazelWorkspaceWithManagedDependencies(dependencyNodesFrom(originalSingleDependency))

      val divergentSingleDependency = SingleDependency(managedDependency, divergentTransitiveDependency)

      val resolver = givenFakeResolverForDependencies(Set(originalSingleDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), dependencyNodesFrom(divergentSingleDependency))

      localWorkspace must includeImportExternalTargetWith(
        artifact = managedDependency.coordinates,
        compileTimeDependenciesIgnoringVersion = Set(divergentTransitiveDependency.coordinates), thirdPartyPath = thirdPartyPath)

      localWorkspace must includeImportExternalTargetWith(divergentTransitiveDependency.coordinates, thirdPartyPath = thirdPartyPath)
    }

    "not persist jar import of dependency nor of transitive dependency, because managed has the same depednecies " +
      "even though resolver has the same declared dependency with a divergent transitive dependency" in new baseCtx {
      val originalSingleDependency = SingleDependency(managedDependency, transitiveDependency)

      givenBazelWorkspaceWithManagedDependencies(dependencyNodesFrom(originalSingleDependency))

      val divergentTransitiveDependency = transitiveDependency.withVersion("new-version")
      val divergentSingleDependency = SingleDependency(managedDependency, divergentTransitiveDependency)

      val resolver = givenFakeResolverForDependencies(Set(divergentSingleDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), dependencyNodesFrom(originalSingleDependency))

      localWorkspace must notIncludeImportExternalRulesInWorkspace(managedDependency.coordinates)
      localWorkspace must notIncludeImportExternalRulesInWorkspace(transitiveDependency.coordinates)
    }

    "not persist managed dependencies not found in local deps" in new baseCtx {
      val localDependency = aDependency("local")

      givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(managedDependency))

      val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(localDependency)))

      localWorkspace must notIncludeImportExternalRulesInWorkspace(managedDependency.coordinates)
    }

    "not persist jar import even if has different scope" in new baseCtx {
      givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(managedDependency))

      val divergentDependency = managedDependency.withScope(MavenScope.Test)

      val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentDependency)))

      localWorkspace must notIncludeImportExternalRulesInWorkspace(divergentDependency.coordinates)
    }

    "persist dependency's jar import without its exclusion. do not persist java import exclusion" in new baseCtx {
      val managedSingleDependency = SingleDependency(managedDependency, transitiveDependency)
      givenBazelWorkspaceWithManagedDependencies(dependencyNodesFrom(managedSingleDependency))

      val transitiveCoordinates: Coordinates = transitiveDependency.coordinates
      val someExclusion = Exclusion(transitiveCoordinates.groupId, transitiveCoordinates.artifactId)

      val divergentDependency = managedDependency.withExclusions(Set(someExclusion))
      val resolver = givenFakeResolverForDependencies(singleDependencies = Set(managedSingleDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentDependency), aRootDependencyNode(transitiveDependency)))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentDependency.coordinates,
        exclusions = Set(someExclusion),
        thirdPartyPath = thirdPartyPath
      )

      localWorkspace must notIncludeImportExternalRulesInWorkspace(transitiveDependency.coordinates)
    }

    // this is needed only for "phase 1" - once all internal wix dependencies will be source dependencies these pom scala_import targets will be redundant
    "persist a pom dependency only as a scala_import " in new baseCtx {
      val aManagedDependency = aPomArtifactDependency("existing")

      givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(aManagedDependency))

      val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
      val synchronizer = givenSynchornizerFor(resolver)

      synchronizer.sync(userAddedDependencies = Set(), localNodes = Set(aRootDependencyNode(aManagedDependency)))

      localWorkspace must includeLibraryRuleTarget(
        aManagedDependency.coordinates,
        LibraryRule.pomLibraryRule(artifact = aManagedDependency.coordinates, Set.empty, Set.empty, Set.empty))
    }

    "persist jar import with sha256" in new resolvedCtx {
      val someChecksum = "checksum"
      val synchronizer = givenSynchornizerFor(resolver, remoteStorageWillReturn(Some(someChecksum)))

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentDependency)))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentDependency.coordinates,
        checksum = Some(someChecksum),
        thirdPartyPath = thirdPartyPath
      )
    }

    "persist SNAPSHOT jar import without sha256" in new resolvedCtx {
      val divergentSnapshotDependency = managedDependency.withVersion("2.1193.0-SNAPSHOT")

      val synchronizer = givenSynchornizerFor(resolver, remoteStorageWillReturn(Some("checksum")))

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentSnapshotDependency)))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentSnapshotDependency.coordinates,
        checksum = None,
        thirdPartyPath = thirdPartyPath
      )
    }

    "persist SNAPSHOT jar import with snapshot sources True" in new resolvedCtx {
      val divergentSnapshotDependency = managedDependency.withVersion("2.1193.0-SNAPSHOT")

      val synchronizer = givenSynchornizerFor(resolver,
        remoteStorageWillReturn(checksum = Some("checksum"), srcChecksum = Some("src_checksum")))

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentSnapshotDependency)))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentSnapshotDependency.coordinates,
        checksum = None,
        srcChecksum = None,
        snapshotSources = true,
        thirdPartyPath = thirdPartyPath
      )
    }

    "persist jar import with source jar" in new resolvedCtx {
      val synchronizer = givenSynchornizerFor(resolver,
        remoteStorageWillReturn(checksum = Some("checksum"), srcChecksum = Some("src_checksum")))

      synchronizer.sync(userAddedDependencies = Set(), Set(aRootDependencyNode(divergentDependency)))

      localWorkspace must includeImportExternalTargetWith(
        artifact = divergentDependency.coordinates,
        checksum = Some("checksum"),
        srcChecksum = Some("src_checksum"),
        thirdPartyPath = thirdPartyPath
      )
    }
  }

  trait baseCtx extends Scope {
    private val externalWorkspaceName = "some_external_workspace_name"
    private val externalFakeLocalWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = externalWorkspaceName)
    val externalFakeBazelRepository = new InMemoryBazelRepository(externalFakeLocalWorkspace)
    private val targetFakeLocalWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = localWorkspaceName)
    val targetFakeBazelRepository = new InMemoryBazelRepository(targetFakeLocalWorkspace)
    val importExternalLoadStatement = ImportExternalLoadStatement(importExternalRulePath = "@some_workspace//:import_external.bzl", importExternalMacroName = "some_import_external")
    val gitAdder = new FakeGitAdder()

    val localWorkspace = new BazelWorkspaceDriver(targetFakeLocalWorkspace)

    val managedDependency = aDependency("base")
    val transitiveDependency = aDependency("transitive").withScope(MavenScope.Runtime)

    def givenBazelWorkspaceWithManagedDependencies(managedDeps: DependencyNode*): Unit = {
      new BazelDependenciesWriter(
        externalFakeLocalWorkspace,
        importExternalLoadStatement = importExternalLoadStatement).writeDependencies(managedDeps.map(_.toBazelNode).toSet)
    }

    def givenBazelWorkspaceWithManagedDependencies(managedDeps: Set[DependencyNode]): Unit = {
      givenBazelWorkspaceWithManagedDependencies(managedDeps.toSeq: _*)
    }

    def givenSynchornizerFor(resolver: FakeMavenDependencyResolver, storage: DependenciesRemoteStorage = _ => None, gitAdder: Option[GitAdder] = None) = {
      val neverLinkResolver = NeverLinkResolver()
      new DiffSynchronizer(
        Some(externalFakeBazelRepository),
        targetFakeBazelRepository,
        resolver,
        storage,
        neverLinkResolver,
        importExternalLoadStatement,
        gitAdder
      )
    }
  }

  trait resolvedCtx extends baseCtx {
    givenBazelWorkspaceWithManagedDependencies(aRootDependencyNode(managedDependency))

    val divergentDependency = managedDependency.withVersion("new-version")

    val resolver = givenFakeResolverForDependencies(rootDependencies = Set(managedDependency))
  }

}


class FakeGitAdder() extends GitAdder(null) {
  var addedFiles: Set[String] = Set()

  override def addFiles(fileSet: Set[String]): Unit = addedFiles = fileSet
}


