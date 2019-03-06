package com.wix.build.sync

import com.wix.build.sync.api.{BazelManagedDepsSyncEnded, ThirdPartyArtifact}
import com.wix.build.bazel.{BazelDependenciesReader, BazelRepository}
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates}
import com.wix.ci.greyhound.events.BuildFinished
import com.wix.greyhound.DetailedProduceResult
import com.wix.greyhound.producer.ProduceTarget
import com.wix.greyhound.producer.builder.GreyhoundResilientProducer
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class DependencyUpdateHandler(managedDependenciesUpdate: ManagedDependenciesUpdateHandler,
                              producerToSynchronizedManagedDepsTopic: GreyhoundResilientProducer,
                              managedDepsSyncFinished : ManagedDepsSyncFinished ) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def handleBuildMessage(message: BuildFinished): Unit = {
    logger.info(s"Got Build message: $message")
    producerToSynchronizedManagedDepsTopic.produce(message, ProduceTarget.toKey("key"))
  }

  def handleMessageFromSynchronizedManagedDepsTopic(message: BuildFinished): Unit = {
    logger.info(s"Got synchronized ManagedDeps message $message")
    managedDependenciesUpdate.run
    managedDepsSyncFinished.publishEvent()
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



class ManagedDepsSyncFinished(managedDepsBazelRepository: BazelRepository,syncEndedProducer : GreyhoundResilientProducer){
  def publishEvent(): Future[DetailedProduceResult] = {
    val thirdPartyManagedArtifacts = readManagedArtifacts()
    syncEndedProducer.produce(BazelManagedDepsSyncEnded(thirdPartyManagedArtifacts))
  }

  private def readManagedArtifacts() : Set[ThirdPartyArtifact] = {
    val managedDepsRepoReader = new BazelDependenciesReader(managedDepsBazelRepository.localWorkspace())
    managedDepsRepoReader.allDependenciesAsMavenDependencyNodes()
      .map(d=>ThirdPartyArtifact(d.baseDependency.coordinates.groupId,
        d.baseDependency.coordinates.artifactId,
        d.baseDependency.coordinates.version,
        d.baseDependency.coordinates.packaging.value,
        d.baseDependency.coordinates.classifier,
        None))
  }
}

