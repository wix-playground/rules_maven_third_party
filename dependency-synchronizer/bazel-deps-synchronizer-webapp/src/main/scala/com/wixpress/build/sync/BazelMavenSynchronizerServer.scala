package com.wix.build.sync

import com.wix.bootstrap.jetty.BootstrapServer
import com.wix.build.sync.api.BazelSyncGreyhoundEvents
import com.wix.build.maven.Coordinates
import com.wix.ci.greyhound.events._
import com.wix.framework.spring.JsonRpcServerConfiguration
import com.wix.greyhound._
import com.wix.greyhound.producer.builder.ResilientProducerMaker
import com.wix.hoopoe.json.JsonMapper
import com.wix.vi.githubtools.masterguard.enforceadmins.MasterEnforcer
import com.wix.vi.githubtools.masterguard.spring.EnforceAdminsSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration, Import}


object BazelMavenSynchronizerServer extends BootstrapServer {
  override def additionalSpringConfig = Some(classOf[SynchronizerConfiguration])
}

@Configuration
@Import(Array(classOf[JsonRpcServerConfiguration], classOf[GreyhoundSpringConfig], classOf[EnforceAdminsSpringConfig]))
class SynchronizerConfiguration {
  
  private val configuration = BazelMavenSynchronizerConfig.root

  private val buildTopic = TeamcityTopic.TeamcityEvents

  private val dependencyManagementArtifact: Coordinates = Coordinates.deserialize(configuration.dependencyManagementArtifact)

  private def buildFinishedMessage= (buildFinished: BuildFinished) => {
    buildFinished.isSuccessful &&
      buildFinished.buildConfigId == configuration.dependencyManagementArtifactBuildTypeId
  }

  @Bean
  def synchronizedDependencyUpdateHandler(producers: Producers, resilientMaker: ResilientProducerMaker,
                                          masterEnforcer: MasterEnforcer): DependencyUpdateHandler = {
    initTopics()

    val syncEndedProducer = resilientMaker.withTopic(BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic).unordered.build
    producers.add(syncEndedProducer)

    val storage = new ArtifactoryRemoteStorage(configuration.artifactoryUrl, configuration.artifactoryToken)

    val managedDependenciesUpdateHandler = new ManagedDependenciesUpdateHandler(dependencyManagementArtifact,
      configuration.mavenRemoteRepositoryURL,
      storage,
      WixLoadStatements.importExternalLoadStatement,
      configuration.git,
      masterEnforcer,
    )

    //val managedDepsSyncFinished = new ManagedDepsSyncFinished(configuration.git, syncEndedProducer)
    //TODO - connect managedDepsSyncFinished to RepoHippo event on commit to master!

    new DependencyUpdateHandler(managedDependenciesUpdateHandler)
  }

  @Autowired
  def setConsumers(consumers: Consumers,
                   dependencyUpdateHandler: DependencyUpdateHandler): Unit = {
    initTopics()

    setManagedDepsSyncMessagesConsumers(consumers, dependencyUpdateHandler)
  }

  private def initTopics(): Unit = {
    val kafkaAdmin = new KafkaGreyhoundAdmin()
    kafkaAdmin.createTopicIfNotExists(buildTopic)
    kafkaAdmin.createTopicIfNotExists(BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic, partitions = 1)

  }

  private def setManagedDepsSyncMessagesConsumers(consumers: Consumers,
                                          dependencyUpdateHandler: DependencyUpdateHandler) = {
    val buildMessageHandler = MessageHandler
      .aMessageHandler[BuildFinished](dependencyUpdateHandler.handleBuildMessage)
      .withMapper(JsonMapper.global)
      .withFilter(buildFinishedMessage)
      .build
    val buildMessageConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(buildTopic, buildMessageHandler)
      .withGroup("bazel-build")
    consumers.add(buildMessageConsumer)
  }
}
