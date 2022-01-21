package com.wix.build.maven

import com.wix.build.maven.ArtifactDescriptor.anArtifact
import com.wix.build.maven.MavenMakers.{aDependency, randomDependency}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.{AfterEach, Scope}

//noinspection TypeAnnotation
class CoursierDependencyResolverIT extends SpecificationWithJUnit with AfterEach {
  sequential
  val fakeMavenRepository = new FakeMavenRepository()

  "return only one entry for each dependency given transitive dependency has different scope" in new Context {
    override def transitiveRoot = aDependency("transitive").withScope(MavenScope.Runtime)

    val nodes = mavenDependencyResolver.dependencyClosureOf(List(dependency, transitiveRoot.withScope(MavenScope.Compile)), emptyManagedDependencies)
    nodes.filter(_.baseDependency == dependency) must have size 1
    nodes.filter(_.baseDependency.coordinates == transitiveRoot.coordinates) must have size 1
  }

  "given dependency that is not in remote repository must not explode" in new Context {
    val notExistsDependency = randomDependency()

    override def remoteArtifacts: Set[ArtifactDescriptor] = Set.empty

    mavenDependencyResolver.dependencyClosureOf(List(notExistsDependency), emptyManagedDependencies) must beEmpty
  }

  trait Context extends Scope {
    def transitiveRoot = aDependency("transitive")

    def dependency = aDependency("dep")

    def remoteArtifacts: Set[ArtifactDescriptor] = Set(
      anArtifact(dependency.coordinates).withDependency(transitiveRoot),
      anArtifact(transitiveRoot.coordinates)
    )

    def emptyManagedDependencies = List.empty[Dependency]

    def mavenDependencyResolver = resolverBasedOn(remoteArtifacts)
  }

  def resolverBasedOn(artifacts: Set[ArtifactDescriptor]) = {
    fakeMavenRepository.addArtifacts(artifacts)
    fakeMavenRepository.start()
    new CoursierDependencyResolver(List(fakeMavenRepository.url))
  }

  override protected def after = {
    fakeMavenRepository.stop()
  }
}
