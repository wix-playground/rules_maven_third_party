package com.wix.build.sync

import java.io.InputStream

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.wix.bazel.migrator.model.SourceModule
import com.wix.build.bazel.BazelRepository
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates, Dependency, MavenScope}
import com.wix.ci.greyhound.events.{BuildFinished, GATriggeredEvent}
import com.wix.greyhound.producer.ProduceTarget
import com.wix.greyhound.producer.builder.GreyhoundResilientProducer
import org.slf4j.LoggerFactory

class DependencyUpdateHandler(managedDependenciesUpdate: ManagedDependenciesUpdateHandler,
                              frameworkGAUpdateHandler: FrameworkGAUpdateHandler,
                              producerToSynchronizedManagedDepsTopic: GreyhoundResilientProducer,
                              producerToSynchronizedFrameworkLeafTopic: GreyhoundResilientProducer) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def handleBuildMessage(message: BuildFinished): Unit = {
    logger.info(s"Got Build message: $message")
    producerToSynchronizedManagedDepsTopic.produce(message, ProduceTarget.toKey("key"))
  }

  def handleMessageFromSynchronizedManagedDepsTopic(message: BuildFinished): Unit = {
    logger.info(s"Got synchronized ManagedDeps message $message")
    managedDependenciesUpdate.run
  }

  def handleGAMessage(message: GATriggeredEvent): Unit = {
    logger.info(s"Got GA message: $message")
    producerToSynchronizedFrameworkLeafTopic.produce(message, ProduceTarget.toKey("key"))
  }

  def handleMessageFromSynchronizedFrameworkLeafTopic(message: GATriggeredEvent): Unit = {
    logger.info(s"Got synchronized FrameworkLeaf message $message")
    frameworkGAUpdateHandler.run(message.version)
  }
}

class ManagedDependenciesUpdateHandler(dependencyManagementArtifact: Coordinates,
                                       managedDepsBazelRepository: BazelRepository,
                                       mavenRemoteRepositoryURL: List[String],
                                       dependenciesRemoteStorage: DependenciesRemoteStorage) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def run = {
    // resolver has to be re-instantiated on each update, in order to get non-cached version of managed deps snapshot
    val resolver = new AetherMavenDependencyResolver(mavenRemoteRepositoryURL)
    val synchronizer = new BazelMavenSynchronizer(resolver, managedDepsBazelRepository, dependenciesRemoteStorage)

    val managedDependencies = resolver.managedDependenciesOf(dependencyManagementArtifact)

    logger.info(s"retrieved ${managedDependencies.size} managed depednencies from $dependencyManagementArtifact")

    synchronizer.sync(
      dependencyManagementSource = dependencyManagementArtifact,
      dependencies = managedDependencies
    )
  }
}

class FrameworkGAUpdateHandler(serverInfraBazelRepository: BazelRepository,
                               managedDepsBazelRepository: BazelRepository,
                               dependencyManagementArtifact: Coordinates,
                               mavenRemoteRepositoryURL: List[String],
                               dependenciesRemoteStorage: DependenciesRemoteStorage,
                               fwLeafMavenCoordinates: String,
                               randomString: => String) {
  def run(currentBuildVersion: String) = {
    val resolver = new AetherMavenDependencyResolver(mavenRemoteRepositoryURL)

    val fwLeafDependencies = Set(toDependency(Coordinates.deserialize(fwLeafMavenCoordinates).copy(version = currentBuildVersion)))

    val serverInfraModules: Set[Coordinates] = readPreLoadedServerInfraCoordinates

    val synchronizer = new UserAddedDepsDiffSynchronizer(serverInfraBazelRepository,
      managedDepsBazelRepository,
      dependencyManagementArtifact,
      resolver,
      dependenciesRemoteStorage,
      serverInfraModules.map(c => SourceModule("", c)),
      randomString
    )

    synchronizer.syncThirdParties(fwLeafDependencies)
  }

  private def readPreLoadedServerInfraCoordinates = {
    val stream: InputStream = getClass.getResourceAsStream(s"/server-infra-coordinates.json")
    val coordinates = scala.io.Source.fromInputStream(stream).mkString

    val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.readValue(coordinates, classOf[Array[Coordinates]]).toSet
  }

  private def toDependency(coordinates: Coordinates): Dependency = {
    // scope here is of no importance as it is used on third_party and workspace only
    Dependency(coordinates, MavenScope.Compile)
  }
}