package com.wix.build.sync

import com.wix.build.maven.Coordinates
import com.wix.ci.greyhound.events.BuildFinished
import com.wix.greyhound.GreyhoundProducer
import org.slf4j.LoggerFactory

class DependencyUpdateHandler(synchronizer: BazelMavenSynchronizer,
                              dependencyManagementArtifact: Coordinates,
                              producerToSynchronizedTopic: GreyhoundProducer) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def handleMessageFromCI(message: BuildFinished): Unit = {
    logger.info(s"Got message: $message")
    producerToSynchronizedTopic.produce("key", message)
  }

  def handleMessageFromSynchronizedTopic(message: BuildFinished): Unit = {
    logger.info(s"Got synchronized message $message")
    synchronizer.sync(dependencyManagementArtifact)
  }

}
