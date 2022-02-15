package com.wix.build.maven

import com.wix.build.maven._
import com.wix.build.maven.CoursierDependencyResolver._
import coursier.MavenRepository
import coursier.cache.Cache
import coursier.core.{Attributes, Classifier, Configuration, Module, ModuleName, Organization, Resolution, ResolutionProcess, Type, Dependency => CoursierDependency}

import scala.concurrent.ExecutionContext.Implicits.global


class CoursierDependencyResolver(remoteRepoURLs: => List[String]) extends MavenDependencyResolver {
  override def managedDependenciesOf(artifact: Coordinates): List[Dependency] = ???

  override def directDependenciesOf(artifact: Coordinates): List[Dependency] = ???


  override def dependencyClosureOf(baseDependencies: List[Dependency], withManagedDependencies: List[Dependency], ignoreMissingDependencies: Boolean): Set[DependencyNode] = {
    val repositories = remoteRepoURLs.map(repo => MavenRepository(repo))
    val dependencies = (baseDependencies ++ withManagedDependencies).map(toCoursierDependency)

    val fetch = ResolutionProcess.fetch(repositories, Cache.default.fetch)

    val resolution = Resolution()
      .withRootDependencies(dependencies)

    // not found packages are under result.errors
    val result = resolution.process.run(fetch).unsafeRun()

    result.finalDependenciesCache.map(toDependencyNode).toSet
  }

}

object CoursierDependencyResolver {

  def toDependencyNode(result: (CoursierDependency, Seq[CoursierDependency])): DependencyNode = {
    DependencyNode(toDependency(result._1), result._2.map(toDependency).toSet)
  }

  def toCoursierModule(dependency: Dependency): Module = {
    Module(Organization(dependency.coordinates.groupId), ModuleName(dependency.coordinates.artifactId), Map.empty)
  }

  def toCoursierDependency(dependency: Dependency): CoursierDependency =
    CoursierDependency(toCoursierModule(dependency), dependency.version)
      .withConfiguration(Configuration(dependency.scope.name))
      .withExclusions(dependency.exclusions.map(toCoursierExclusion))
      .withAttributes(Attributes(Type(dependency.coordinates.packaging.value), dependency.coordinates.classifier.map(Classifier(_)).getOrElse(Classifier.empty)))

  def toCoordinates(dependency: CoursierDependency): Coordinates = {
    val packaging = if (dependency.attributes.packaging.value.nonEmpty) Packaging(dependency.attributes.packaging.value) else Packaging("jar")
    val classifier = if (dependency.attributes.classifier.value.nonEmpty) Some(dependency.attributes.classifier.value) else None
    Coordinates(dependency.module.organization.value, dependency.module.name.value, dependency.version, packaging, classifier)
  }

  def toExclusion(coursierExclusion: (Organization, ModuleName)): Exclusion = {
    Exclusion(coursierExclusion._1.value, coursierExclusion._2.value)
  }

  def toCoursierExclusion(exclusion: Exclusion): (Organization, ModuleName) = {
    (Organization(exclusion.groupId), ModuleName(exclusion.artifactId))
  }

  def toDependency(dependency: CoursierDependency): Dependency = {
    Dependency(toCoordinates(dependency), MavenScope.of(dependency.configuration.value), false, exclusions = dependency.exclusions.map(toExclusion))
  }
}
