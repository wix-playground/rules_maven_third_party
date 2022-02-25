package com.wix.build.sync

import com.wix.build.bazel.ImportExternalRule._
import com.wix.build.bazel.ThirdPartyOverridesMakers.{overrideCoordinatesFrom, runtimeOverrides}
import com.wix.build.bazel._
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven._
import com.wix.build.sync.DependenciesRemoteStorageTestSupport.remoteStorageWillReturn
import com.wix.build.BazelWorkspaceDriver._
import com.wix.build.{BazelWorkspaceDriver, MavenJarInBazel}
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class BazelMavenManagedDepsSynchronizerAcceptanceTest extends SpecificationWithJUnit {

  val thirdPartyPath = "third_party"

  "Bazel Maven Synchronizer," >> {
    "when asked to sync one maven root dependency" should {
      "update maven jar version in bazel based repo" in new baseCtx {
        val existingDependency = aDependency("existing").withVersion("old-version")
        givenBazelWorkspaceWithDependency(rootMavenJarFrom(existingDependency))
        val updatedDependency = existingDependency.withVersion("new-version")
        val updatedResolver = updatedDependenciesResolverWithManagedArtifacts(
          artifacts = Set(ArtifactDescriptor.rootFor(updatedDependency.coordinates))
        )

        syncBasedOn(updatedResolver)

        bazelWorkspace.versionOfImportedJar(existingDependency.coordinates) must beSome(updatedDependency.version)
      }

      "insert new maven jar to bazel based repo" in new blankBazelWorkspaceAndNewManagedRootDependency {
        syncBasedOn(updatedResolver)

        bazelWorkspace.versionOfImportedJar(newDependency.coordinates) must beSome(newDependency.version)
      }

      "add new target in import external file under third_party" in new blankBazelWorkspaceAndNewManagedRootDependency {
        syncBasedOn(updatedResolver)

        bazelWorkspace must includeImportExternalTargetWith(artifact = newDependency.coordinates, runtimeDependencies = Set.empty, thirdPartyPath = thirdPartyPath)
      }

      "make sure new BUILD.bazel files in third_parties has appropriate header" in new baseCtx {
        val pomDependency = aPomArtifactDependency("some-artifact")
        val updatedResolver = updatedDependenciesResolverWithManagedArtifacts(
          artifacts = Set(ArtifactDescriptor.rootFor(pomDependency.coordinates))
        )
        syncBasedOn(updatedResolver)

        val buildFileContent = fakeLocalWorkspace.buildFileContent(LibraryRule.packageNameBy(pomDependency.coordinates, thirdPartyPath))

        buildFileContent must beSome
        buildFileContent.get must startWith(BazelBuildFile.DefaultHeader)
      }

      "persist the change with proper message" in new blankBazelWorkspaceAndNewManagedRootDependency {
        syncBasedOn(updatedResolver)

        val expectedChange = Change(
          message =
            s"""${BazelMavenManagedDepsSynchronizer.ManagedDepsUpdateCommitMsg}
               |#pr""".stripMargin
        )

        fakeBazelRepository.allChangesInBranch(someBranchName) must contain(matchTo(expectedChange))
      }

      "persist jar import with sha256" in new blankBazelWorkspaceAndNewManagedRootDependency {
        val someChecksum = "checksum"
        syncBasedOn(updatedResolver, remoteStorageWillReturn(Some(someChecksum)))

        bazelWorkspace must includeImportExternalTargetWith(artifact = newDependency.coordinates, runtimeDependencies = Set.empty, checksum = Some(someChecksum), thirdPartyPath = thirdPartyPath)
      }

    }
    "when asked to sync one maven dependency that has dependencies" should {

      "update maven jar version for it and for its direct dependency" in new baseCtx {
        givenBazelWorkspaceWithDependency(
          rootMavenJarFrom(transitiveDependency),
          basicArtifactWithRuntimeDependency(baseDependency.coordinates, transitiveDependency.coordinates)
        )

        val updatedBaseDependency = baseDependency.withVersion("new-version")
        val updatedTransitiveDependency = transitiveDependency.withVersion("new-version")
        val updatedJarArtifact = ArtifactDescriptor.withSingleDependency(updatedBaseDependency.coordinates,
          updatedTransitiveDependency)
        val updatedDependencyArtifact = ArtifactDescriptor.rootFor(updatedTransitiveDependency.coordinates)
        val updatedResolver = updatedDependencyResolverWith(
          artifacts = Set(updatedJarArtifact, updatedDependencyArtifact),
          managedDependencies = Set(updatedBaseDependency)
        )

        syncBasedOn(updatedResolver)

        bazelWorkspace.versionOfImportedJar(baseDependency.coordinates) must beSome(updatedBaseDependency.version)
        bazelWorkspace.versionOfImportedJar(transitiveDependency.coordinates) must beSome(updatedTransitiveDependency.version)

      }

      "reflect runtime dependencies in appropriate third_party target" in new blankBazelWorkspaceAndNewManagedArtifactWithDependency {

        syncBasedOn(updatedResolver)

        bazelWorkspace must includeImportExternalTargetWith(
          artifact = baseDependency.coordinates,
          runtimeDependencies = Set(transitiveDependency.coordinates),
          thirdPartyPath = thirdPartyPath
        )
      }

      "reflect exclusion in appropriate third_party target" in new baseCtx {
        givenNoDependenciesInBazelWorkspace()

        val someExclusion = Exclusion("some.ex.group", "some-excluded-artifact")
        val baseDependencyArtifact = ArtifactDescriptor.withSingleDependency(baseDependency.coordinates,
          transitiveDependency.withScope(MavenScope.Runtime))
        val dependencyJarArtifact = ArtifactDescriptor.rootFor(transitiveDependency.coordinates)
        val updatedResolver = updatedDependencyResolverWith(
          artifacts = Set(baseDependencyArtifact, dependencyJarArtifact),
          managedDependencies = Set(baseDependency.withExclusions(Set(someExclusion)))
        )

        syncBasedOn(updatedResolver)

        bazelWorkspace must includeImportExternalTargetWith(artifact = baseDependency.coordinates,
          runtimeDependencies = Set(transitiveDependency.coordinates),
          exclusions = Set(someExclusion),
          thirdPartyPath = thirdPartyPath
        )
      }

      "reflect third party overrides in appropriate third_party target" in new baseCtx {
        val injectedCoordinates: Coordinates = someCoordinates("some_label")
        givenBazelWorkspace(
          mavenJarsInBazel = Set.empty,
          overrides = runtimeOverrides(overrideCoordinatesFrom(baseDependency.coordinates), jarLabelBy(injectedCoordinates))
        )
        val baseJarArtifact = ArtifactDescriptor.rootFor(baseDependency.coordinates)
        val dependencyJarArtifact = ArtifactDescriptor.rootFor(transitiveDependency.coordinates)
        val updatedResolver = updatedDependencyResolverWith(
          artifacts = Set(baseJarArtifact),
          managedDependencies = Set(baseDependency)
        )

        syncBasedOn(updatedResolver)

        bazelWorkspace must includeImportExternalTargetWith(
          artifact = baseDependency.coordinates,
          runtimeDependencies = Set(injectedCoordinates),
          thirdPartyPath = thirdPartyPath
        )
      }

      "create appropriate third_party target for the new transitive dependency" in new blankBazelWorkspaceAndNewManagedArtifactWithDependency {
        syncBasedOn(updatedResolver)

        bazelWorkspace must includeImportExternalTargetWith(artifact = transitiveDependency.coordinates, runtimeDependencies = Set.empty, thirdPartyPath = thirdPartyPath)
      }


      "update appropriate third_party target for updated jar that introduced new dependency" in new baseCtx {
        givenBazelWorkspaceWithDependency(rootMavenJarFrom(baseDependency))
        val updatedBaseDependency = baseDependency.withVersion("new-version")
        val updatedJarArtifact = ArtifactDescriptor.withSingleDependency(updatedBaseDependency.coordinates, transitiveDependency)
        val dependencyArtifact = ArtifactDescriptor.rootFor(transitiveDependency.coordinates)
        val updatedResolver = updatedDependencyResolverWith(
          artifacts = Set(updatedJarArtifact, dependencyArtifact),
          managedDependencies = Set(updatedBaseDependency)
        )

        syncBasedOn(updatedResolver)

        bazelWorkspace must includeImportExternalTargetWith(artifact = transitiveDependency.coordinates, runtimeDependencies = Set.empty, thirdPartyPath = thirdPartyPath)
      }

      "ignore provided/test scope dependencies for appropriate third_party target" in new baseCtx {
        val otherTransitiveDependency = aDependency("other-transitive")
        givenNoDependenciesInBazelWorkspace()
        val baseJarArtifact = ArtifactDescriptor.anArtifact(
          baseDependency.coordinates,
          List(
            transitiveDependency.withScope(MavenScope.Provided),
            otherTransitiveDependency.withScope(MavenScope.Test))
        )
        val dependencyJarArtifact = ArtifactDescriptor.rootFor(transitiveDependency.coordinates)
        val otherDependencyJarArtifact = ArtifactDescriptor.rootFor(otherTransitiveDependency.coordinates)
        val updatedResolver = updatedDependencyResolverWith(
          artifacts = Set(baseJarArtifact, dependencyJarArtifact, otherDependencyJarArtifact),
          managedDependencies = Set(baseDependency)
        )
        val synchronizer = bazelMavenSynchronizerFor(updatedResolver, fakeBazelRepository)

        synchronizer.sync(dependencyManagementCoordinates, someBranchName)

        bazelWorkspace must includeImportExternalTargetWith(
          artifact = baseDependency.coordinates,
          runtimeDependencies = Set.empty,
          thirdPartyPath = thirdPartyPath
        )
      }

      "reflect compile time scope dependencies for appropriate third_party target" in new baseCtx {
        givenNoDependenciesInBazelWorkspace()
        val baseJarArtifact = ArtifactDescriptor.withSingleDependency(
          baseDependency.coordinates,
          transitiveDependency.withScope(MavenScope.Compile)
        )
        val dependencyJarArtifact = ArtifactDescriptor.rootFor(transitiveDependency.coordinates)
        val updatedResolver = updatedDependencyResolverWith(
          artifacts = Set(baseJarArtifact, dependencyJarArtifact),
          managedDependencies = Set(baseDependency)
        )
        val synchronizer = bazelMavenSynchronizerFor(updatedResolver, fakeBazelRepository)

        synchronizer.sync(dependencyManagementCoordinates, someBranchName)

        bazelWorkspace must includeImportExternalTargetWith(
          artifact = baseDependency.coordinates,
          compileTimeDependenciesIgnoringVersion = Set(transitiveDependency.coordinates),
          runtimeDependencies = Set.empty,
          thirdPartyPath = thirdPartyPath
        )
      }

      "bound version of transitive dependency according to managed dependencies" in new baseCtx {
        val transitiveManagedDependency = transitiveDependency.withVersion("managed")
        val updatedResolver = updatedDependencyResolverWith(
          managedDependencies = Set(baseDependency, transitiveManagedDependency),
          artifacts = Set(
            baseDependency.asArtifactWithSingleDependency(transitiveDependency),
            transitiveDependency.asRootArtifact,
            transitiveManagedDependency.asRootArtifact)
        )

        syncBasedOn(updatedResolver)

        bazelWorkspace.versionOfImportedJar(transitiveDependency.coordinates) must beSome(transitiveManagedDependency.version)
      }


    }
  }
  //why is fakeBazelRepository hardly used
  //many tests feel like they're hiding detail

  private def basicArtifactWithRuntimeDependency(jar: Coordinates, runtimeDependency: Coordinates) =
    MavenJarInBazel(
      artifact = jar,
      runtimeDependencies = Set(runtimeDependency),
      compileTimeDependencies = Set.empty,
      exclusions = Set.empty
    )


  private def rootMavenJarFrom(dependency: Dependency) = {
    MavenJarInBazel(
      artifact = dependency.coordinates,
      runtimeDependencies = Set.empty,
      compileTimeDependencies = Set.empty,
      exclusions = Set.empty
    )
  }

  private implicit class CoordinatesExtended(coordinates: Coordinates) {
    def asDependency: Dependency = Dependency(coordinates, MavenScope.Compile)

    def asRootArtifact: ArtifactDescriptor = ArtifactDescriptor.rootFor(coordinates)
  }

  private def matchTo(change: Change): Matcher[Change] = {
    ((_: Change).message) ^^ beEqualTo(change.message)
  }

  abstract class blankBazelWorkspaceAndNewManagedArtifactWithDependency extends baseCtx {
    givenNoDependenciesInBazelWorkspace()

    val baseJarArtifact = ArtifactDescriptor.withSingleDependency(
      coordinates = baseDependency.coordinates,
      dependency = transitiveDependency.copy(scope = MavenScope.Runtime))
    val dependencyJarArtifact = ArtifactDescriptor.rootFor(transitiveDependency.coordinates)
    val updatedResolver = updatedDependencyResolverWith(
      managedDependencies = Set(baseDependency),
      artifacts = Set(baseJarArtifact, dependencyJarArtifact)
    )
  }

  trait blankBazelWorkspaceAndNewManagedRootDependency extends baseCtx {
    val newDependency = aDependency("new-dep")
    givenNoDependenciesInBazelWorkspace()
    val newArtifact = ArtifactDescriptor.rootFor(newDependency.coordinates)
    val updatedResolver = updatedDependenciesResolverWithManagedArtifacts(artifacts = Set(newArtifact))
  }

  trait baseCtx extends Scope {
    val fakeLocalWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = "some_local_workspace_name")
    val fakeBazelRepository = new InMemoryBazelRepository(fakeLocalWorkspace)
    val bazelWorkspace = new BazelWorkspaceDriver(fakeLocalWorkspace)
    val importExternalLoadStatement = ImportExternalLoadStatement(importExternalRulePath = "@some_workspace//:import_external.bzl", importExternalMacroName = "some_import_external")

    val baseDependency = aDependency("base")
    val transitiveDependency = aDependency("transitive")
    val dependencyManagementCoordinates = Coordinates("some.group", "deps-management", "1.0", Packaging("pom"))

    val someBranchName = "someString"

    def givenBazelWorkspaceWithDependency(mavenJarInBazel: MavenJarInBazel*) = {
      givenBazelWorkspace(mavenJarInBazel.toSet)
    }

    def givenBazelWorkspace(mavenJarsInBazel: Set[MavenJarInBazel] = Set.empty, overrides: ThirdPartyOverrides = ThirdPartyOverrides.empty) = {
      bazelWorkspace.writeDependenciesAccordingTo(mavenJarsInBazel)
      fakeLocalWorkspace.setThirdPartyOverrides(overrides)
    }

    def updatedDependenciesResolverWithManagedArtifacts(artifacts: Set[ArtifactDescriptor]) = {
      val artifactsAsDeps = artifacts.map(a => Dependency(a.coordinates, MavenScope.Compile))
      val dependencyManagementArtifact = ArtifactDescriptor.anArtifact(dependencyManagementCoordinates, List.empty, artifactsAsDeps.toList)
      new FakeMavenDependencyResolver(artifacts + dependencyManagementArtifact)
    }

    def updatedDependencyResolverWith(managedDependencies: Set[Dependency] = Set.empty, artifacts: Set[ArtifactDescriptor]) = {
      val dependencyManagementArtifact = ArtifactDescriptor.anArtifact(dependencyManagementCoordinates, List.empty, managedDependencies.toList)
      new FakeMavenDependencyResolver(artifacts + dependencyManagementArtifact)
    }

    def bazelMavenSynchronizerFor(resolver: FakeMavenDependencyResolver, fakeBazelRepository: InMemoryBazelRepository, storage: DependenciesRemoteStorage = _ => None) = {
      new BazelMavenManagedDepsSynchronizer(resolver, fakeBazelRepository, storage, importExternalLoadStatement)
    }

    def syncBasedOn(resolver: FakeMavenDependencyResolver, storage: DependenciesRemoteStorage = _ => None) = {
      val synchronizer = new BazelMavenManagedDepsSynchronizer(resolver, fakeBazelRepository, storage, importExternalLoadStatement)
      synchronizer.sync(dependencyManagementCoordinates, someBranchName)
    }

    protected def givenNoDependenciesInBazelWorkspace() = {
      givenBazelWorkspaceWithDependency()
    }
  }

  private implicit class `Dependency to Artifact`(baseDependency: Dependency) {
    def asRootArtifact: ArtifactDescriptor = ArtifactDescriptor.rootFor(baseDependency.coordinates)

    def asArtifactWithSingleDependency(dependency: Dependency): ArtifactDescriptor =
      ArtifactDescriptor.withSingleDependency(baseDependency.coordinates, dependency)
  }

}