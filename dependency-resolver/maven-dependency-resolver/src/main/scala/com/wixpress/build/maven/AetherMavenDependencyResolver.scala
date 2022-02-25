package com.wix.build.maven

import java.nio.file.{Files, Path}

import better.files.File
import com.wix.build.maven.resolver.ManualRepositorySystemFactory
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.artifact.{Artifact, DefaultArtifact}
import org.eclipse.aether.collection.{CollectRequest, CollectResult}
import org.eclipse.aether.graph.{Dependency => AetherDependency, DependencyNode => AetherDependencyNode}
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository.{Builder => RemoteRepositoryBuilder}
import org.eclipse.aether.resolution.{ArtifactDescriptorRequest, ArtifactDescriptorResult}
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils
import org.eclipse.aether.util.graph.transformer.ConflictResolver
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy

import scala.collection.JavaConverters._

class AetherMavenDependencyResolver(remoteRepoURLs: => List[String]) extends MavenDependencyResolver {

  private val repositorySystem = ManualRepositorySystemFactory.newRepositorySystem

  private val tmpLocalRepoPath = {
    val tempDir = File.newTemporaryDirectory("local-repo")
    tempDir.toJava.deleteOnExit()
    tempDir
  }

  override def managedDependenciesOf(artifact: Coordinates): Set[Dependency] = {
      val artifactDescriptor = artifactDescriptorOf(descriptorRequest(artifact))
      dependenciesSetFrom(artifactDescriptor.getManagedDependencies.asScala)
  }

  private def artifactDescriptorOf(request: ArtifactDescriptorRequest):ArtifactDescriptorResult  =
    withSession(repositorySystem.readArtifactDescriptor(_, request))

  override def directDependenciesOf(coordinates: Coordinates): Set[Dependency] =
      directDependenciesOf(artifactFromCoordinates(coordinates))

  def directDependenciesOf(pathToPom: Path): Set[Dependency] =
    directDependenciesOf(artifactFromPath(pathToPom))

  private def directDependenciesOf(artifact: DefaultArtifact) = {
    val artifactDescriptor = artifactDescriptorOf(descriptorRequest(artifact))
    dependenciesSetFrom(artifactDescriptor.getDependencies.asScala)
  }

  private def artifactFromCoordinates(artifact: Coordinates) =
    new DefaultArtifact(artifact.groupId, artifact.artifactId, artifact.packaging.getOrElse(""), artifact.version)

  private def artifactFromPath(pathToPom: Path) = {
    val project = (new MavenXpp3Reader).read(Files.newBufferedReader(pathToPom))
    val version = (maybeVersionFromPom(project) orElse maybeVersionFromParent(project))
      .getOrElse(throw new RuntimeException("could not parse version from pom"))
    val groupId = (Option(project.getGroupId) orElse Option(project.getParent).map(_.getGroupId))
      .getOrElse(throw new RuntimeException("could not parse groupId from pom"))
    val artifact = new DefaultArtifact(groupId, project.getArtifactId, "", version)
    artifact
  }

  private def maybeVersionFromPom(project: Model) = {
    Option(project.getVersion)
  }

  private def maybeVersionFromParent(project: Model) = {
    Option(project.getParent).map(_.getVersion)
  }

  override def dependencyClosureOf(baseDependencies: Set[Dependency], withManagedDependencies: Set[Dependency]): Set[DependencyNode] = {
    withSession(session => {
      prioritizeManagedDeps(session)
      val aetherResponse = repositorySystem.collectDependencies(session, collectRequestOf(baseDependencies, withManagedDependencies))
      dependencyNodesOf(aetherResponse)
        .map(fromAetherDependencyNode)
        .toSet
    })
  }

  private def fromAetherDependencyNode(node: AetherDependencyNode): DependencyNode = {
    DependencyNode(
      baseDependency = Dependency.fromAetherDependency(node.getDependency),
      dependencies = dependenciesSetFromDependencyNodes(node.getChildren.asScala)
    )
  }

  private def dependenciesSetFromDependencyNodes(dependencyNodes: Iterable[AetherDependencyNode]): Set[Dependency] = {
    dependenciesSetFrom(dependencyNodes.map(_.getDependency))
  }

  private def dependenciesSetFrom(dependencies: Iterable[AetherDependency]): Set[Dependency] = {
    dependencies
      .map(Dependency.fromAetherDependency)
      .map(validatedDependency)
      .toSet
  }

  private def validatedDependency(dependency: Dependency): Dependency = {
    import dependency.coordinates._
    if (
      foundTokenIn(groupId) ||
        foundTokenIn(artifactId) ||
        foundTokenIn(version) ||
        packaging.exists(foundTokenIn) ||
        classifier.exists(foundTokenIn)
    ) throw new PropertyNotDefinedException(dependency)
    dependency
  }

  private def foundTokenIn(value: String): Boolean = value.contains("$")

  private def descriptorRequest(of: Coordinates): ArtifactDescriptorRequest = descriptorRequest(of.asAetherArtifact)

  private def descriptorRequest(of: Artifact): ArtifactDescriptorRequest = {
    val artifactReq = new ArtifactDescriptorRequest
    artifactReq.setArtifact(of)
    artifactReq.setRepositories(remoteRepositories)
    artifactReq
  }

  private def withSession[T](f: DefaultRepositorySystemSession => T): T = {
    val localRepo = new LocalRepository(tmpLocalRepoPath.pathAsString)
    val session = MavenRepositorySystemUtils.newSession
    session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true))
    session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo))
    f(session)
  }

  private def remoteRepositories = {
    def mapper(repo: (String, Int)) = {
      val repoURL = repo._1
      val repoIndex = repo._2
      new RemoteRepositoryBuilder(s"repo$repoIndex", "default", repoURL).build()
    }

    val repoList = remoteRepoURLs.zipWithIndex.map(mapper).asJava
    repoList
  }

  private def dependencyNodesOf(collectResult: CollectResult) = {
    val preOrder = new PreorderNodeListGenerator
    val visitor = new RetainNonConflictingDependencyNodesVisitor(preOrder)
    collectResult.getRoot.accept(visitor)
    val list = preOrder.getNodes.asScala
    list
  }

  private def prioritizeManagedDeps(on: DefaultRepositorySystemSession) = {
    on.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true)
    on.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true)
  }

  private def collectRequestOf(baseDependencies: Set[Dependency], withManagedDependencies: Set[Dependency]) = {
    val managedDeps = withManagedDependencies
      .addOrOverride(forcedManagedDependencies)
      .map(_.asAetherDependency)
      .toList.asJava
    val dependencies = baseDependencies.map(_.asAetherDependency).toList.asJava
    (new CollectRequest)
      .setDependencies(dependencies)
      .setManagedDependencies(managedDeps)
      .setRepositories(remoteRepositories)
  }

  private val forcedManagedDependencies = Set(
      Dependency(Coordinates.deserialize("org.reflections:reflections:0.9.10"),MavenScope.Compile)
    )

  private implicit class DependencySetExtended(set:Set[Dependency]){
    def addOrOverride(otherSet:Set[Dependency]):Set[Dependency] = {
      val filteredOriginalSet = set.filterNot(original=>
        otherSet.exists(other=>
          original.coordinates.equalsIgnoringVersion(other.coordinates)))
      filteredOriginalSet ++ otherSet
    }
  }
}

object TestAetherResolver extends App {
  // checks that only runtime/compile scope deps suffice to start that instance
  val aetherResolver = new AetherMavenDependencyResolver(List("https://repo1.maven.org/maven2"))
}