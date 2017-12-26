package com.wix.build.maven

class FakeMavenDependencyResolverTest extends MavenDependencyResolverContract {
  override def resolverBasedOn(artifacts: Set[ArtifactDescriptor]) =
    new FakeMavenDependencyResolver(Set.empty,artifacts)
}
