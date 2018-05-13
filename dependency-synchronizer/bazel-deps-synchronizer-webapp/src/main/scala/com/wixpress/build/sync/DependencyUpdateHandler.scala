package com.wix.build.sync

import com.wix.build.bazel.BazelRepository
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates, MavenDependencyResolver}
import com.wix.ci.greyhound.events.BuildFinished
import com.wix.greyhound.GreyhoundProducer
import org.slf4j.LoggerFactory

class DependencyUpdateHandler(dependencyManagementArtifact: Coordinates,
                              producerToSynchronizedTopic: GreyhoundProducer,
                              bazelRepository: BazelRepository,
                              mavenRemoteRepositoryURL: List[String]) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def handleMessageFromCI(message: BuildFinished): Unit = {
    logger.info(s"Got message: $message")
    producerToSynchronizedTopic.produce("key", message)
  }

  def handleMessageFromSynchronizedTopic(message: BuildFinished): Unit = {
    logger.info(s"Got synchronized message $message")

    // resolver has to be re-instantiated on each update, in order to get non-cached version of managed deps snapshot
    val resolver = new AetherMavenDependencyResolver(mavenRemoteRepositoryURL)
    val synchronizer = new BazelMavenSynchronizer(resolver, bazelRepository)

    val managedDependencies = resolver.managedDependenciesOf(dependencyManagementArtifact)

    logger.info(s"retrieved ${managedDependencies.size} managed depednencies from $dependencyManagementArtifact")

    synchronizer.sync(
      dependencyManagementSource = dependencyManagementArtifact,
      dependencies = managedDependencies
    )
  }

}
