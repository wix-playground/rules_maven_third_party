package com.wix.build.bazel

import com.wix.build.BazelWorkspaceDriver
import com.wix.build.BazelWorkspaceDriver._
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven._
import com.wix.build.maven.resolver.aether.AetherMavenDependencyResolver
import com.wix.build.sync.{DiffSynchronizer, FakeGitAdder}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class DiffSynchronizerIT extends SpecificationWithJUnit {
  sequential

  val thirdPartyPath = "third_party"

  val fakeMavenRepository = new FakeMavenRepository()

  "DiffSynchronizer" should {

    "reflect scope (Runtime) of aether resolved transitive dependency in scala_import target" in new baseCtx {
      val transitiveDependencyRuntimeScope = transitiveDependency.withScope(MavenScope.Runtime)
      val transitiveDependencyCompileScope = transitiveDependencyRuntimeScope.withScope(MavenScope.Compile)

      givenBazelWorkspaceWithManagedDependencies(
        DependencyNode(managedDependency, Set(transitiveDependencyRuntimeScope)),
        aRootDependencyNode(transitiveDependencyRuntimeScope))

      val resolver = givenAetherResolverForDependency(SingleDependency(managedDependency, transitiveDependencyRuntimeScope))
      val synchronizer = givenSynchornizerFor(resolver)


      val resolvedNodes = resolver.dependencyClosureOf(List(managedDependency, transitiveDependencyCompileScope), List.empty)

      synchronizer.sync(Set(), resolvedNodes)

      bazelWorkspace must includeImportExternalTargetWith(
        artifact = managedDependency.coordinates,
        runtimeDependencies = Set(transitiveDependency.coordinates),
        thirdPartyPath = thirdPartyPath
      )

      bazelWorkspace must notIncludeImportExternalRulesInWorkspace(transitiveDependency.coordinates)
    }
  }

  trait baseCtx extends Scope {
    private val externalFakeLocalWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = "some_external_workspace_name")
    val externalFakeBazelRepository = new InMemoryBazelRepository(externalFakeLocalWorkspace)
    private val targetFakeLocalWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = "some_local_workspace_name")
    val targetFakeBazelRepository = new InMemoryBazelRepository(targetFakeLocalWorkspace)
    val importExternalLoadStatement = ImportExternalLoadStatement(importExternalRulePath = "@some_workspace//:import_external.bzl", importExternalMacroName = "some_import_external")

    val bazelWorkspace = new BazelWorkspaceDriver(targetFakeLocalWorkspace)

    val managedDependency = aDependency("base")
    val transitiveDependency = aDependency("transitive")

    def givenBazelWorkspaceWithManagedDependencies(managedDeps: DependencyNode*) = {
      writerFor(externalFakeLocalWorkspace).writeDependencies(managedDeps.map(_.toBazelNode).toSet)
    }

    private def writerFor(localWorkspace: BazelLocalWorkspace, neverLinkResolver: NeverLinkResolver = NeverLinkResolver()) = {
      new BazelDependenciesWriter(localWorkspace,
        neverLinkResolver,
        importExternalLoadStatement = importExternalLoadStatement)
    }

    def givenAetherResolverForDependency(node: SingleDependency) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(node.dependant.coordinates, node.dependency)
      val dependencyDescriptor = ArtifactDescriptor.rootFor(node.dependency.coordinates)

      fakeMavenRepository.addArtifacts(Set(dependantDescriptor, dependencyDescriptor))
      fakeMavenRepository.start()
      new AetherMavenDependencyResolver(List(fakeMavenRepository.url))
    }

    def givenSynchornizerFor(resolver: MavenDependencyResolver) = {
      DiffSynchronizer(Some(externalFakeBazelRepository), targetFakeBazelRepository, resolver, _ => None, NeverLinkResolver(), importExternalLoadStatement, maybeGitAdder = Some(new FakeGitAdder()))
    }

  }

}
