package com.wix.build.sync

import better.files.File
import com.wix.bootstrap.jetty.BootstrapServer
import com.wix.build.bazel.{BazelRepository, GitBazelRepository}
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates, MavenDependencyResolver}
import com.wix.ci.greyhound.events.{BuildFinished, TeamcityTopic}
import com.wix.framework.cache.disk.CacheFolder
import com.wix.framework.cache.spring.CacheFolderConfig
import com.wix.framework.spring.JsonRpcServerConfiguration
import com.wix.greyhound._
import com.wix.hoopoe.json.JsonMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration, Import}

object BazelMavenSynchronizerServer extends BootstrapServer {
  override def additionalSpringConfig = Some(classOf[SynchronizerConfiguration])
}

@Configuration
@Import(Array(classOf[JsonRpcServerConfiguration], classOf[GreyhoundSpringConfig], classOf[CacheFolderConfig]))
class SynchronizerConfiguration {
  private val configuration = BazelMavenSynchronizerConfig.root
  private val ciTopic = TeamcityTopic.TeamcityEvents
  private val dependencyManagementArtifact: Coordinates = Coordinates.deserialize(configuration.dependencyManagementArtifact)
  private val synchronizedTopic = s"${ciTopic}_${dependencyManagementArtifact.serialized.replace(":", "_")}"

  @Bean
  def bazelRepository(artifactAwareCacheFolder: CacheFolder): BazelRepository = {
    val checkoutDirectory = File(artifactAwareCacheFolder.folder.getAbsolutePath) / "clone"
    new GitBazelRepository(
      configuration.git.targetRepoURL,
      checkoutDirectory,
      configuration.git.username,
      configuration.git.email,
      Some(configuration.git.githubToken).filter(!_.isEmpty)
    )
  }

  @Bean
  def producerToSynchronizedTopic(producers: Producers, baseSpec: GreyhoundProducerSpec): GreyhoundProducer = {
    initTopics()
    val producer = baseSpec.withTopic(synchronizedTopic).build
    producers.add(producer)
    producer
  }

  private def messageFilter = (buildFinished: BuildFinished) => {
    buildFinished.isSuccessful && buildFinished.buildConfigId == configuration.dependencyManagementArtifactBuildTypeId
  }

  @Bean
  def synchronizedDependencyUpdateHandler(producerToSynchronizedTopic: GreyhoundProducer,
                                          bazelRepository: BazelRepository): DependencyUpdateHandler =
    new DependencyUpdateHandler(
      dependencyManagementArtifact,
      producerToSynchronizedTopic,
      bazelRepository,
      configuration.mavenRemoteRepositoryURL)


  @Autowired
  def setConsumers(consumers: Consumers,
                   dependencyUpdateHandler: DependencyUpdateHandler): Unit = {
    initTopics()

    val ciMessageHandler = MessageHandler
      .aMessageHandler[BuildFinished](dependencyUpdateHandler.handleMessageFromCI)
      .withMapper(JsonMapper.global)
      .withFilter(messageFilter)
      .build
    val ciMessageConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(ciTopic, ciMessageHandler)
      .withGroup("bazel")

    val synchronizeHandler = MessageHandler
      .aMessageHandler[BuildFinished](dependencyUpdateHandler.handleMessageFromSynchronizedTopic)
      .withMapper(JsonMapper.global)
      .build
    val synchronizeMessageConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(synchronizedTopic, synchronizeHandler)
      .withGroup("deps-synchronizer")
      .withMaxParallelism(1)

    consumers.add(ciMessageConsumer)
    consumers.add(synchronizeMessageConsumer)
  }

  private def initTopics(): Unit = {
    val kafkaAdmin = new KafkaGreyhoundAdmin()
    kafkaAdmin.createTopicIfNotExists(ciTopic)
    kafkaAdmin.createTopicIfNotExists(synchronizedTopic, partitions = 1)
  }
}
