package com.wix.build.sync

import java.util.UUID

import better.files.File
import com.wix.bootstrap.jetty.BootstrapServer
import com.wix.build.bazel.{BazelRepository, GitAuthenticationWithToken, GitBazelRepository}
import com.wix.build.maven.Coordinates
import com.wix.ci.greyhound.events._
import com.wix.framework.cache.disk.CacheFolder
import com.wix.framework.cache.spring.CacheFolderConfig
import com.wix.framework.spring.JsonRpcServerConfiguration
import com.wix.greyhound._
import com.wix.greyhound.producer.builder.ResilientProducerMaker
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

  private val buildTopic = TeamcityTopic.TeamcityEvents
  private val gaTopic = Lifecycle.lifecycleGaTopic

  private val dependencyManagementArtifact: Coordinates = Coordinates.deserialize(configuration.dependencyManagementArtifact)
  private val frameworkLeafArtifact: Coordinates = Coordinates.deserialize(configuration.frameworkLeafArtifact)

  private val synchronizedManagedDepsTopic = s"${buildTopic}_${dependencyManagementArtifact.serialized.replace(":", "_")}"
  private val synchronizedFrameworkLeafTopic = s"${gaTopic}_${frameworkLeafArtifact.serialized.replace(":", "_")}"

  private def buildFinishedMessage= (buildFinished: BuildFinished) => {
    buildFinished.isSuccessful &&
      buildFinished.buildConfigId == configuration.dependencyManagementArtifactBuildTypeId
  }

  private def fwGaTriggeredMessage= (message: BasePromote) => {
    message match {
      case gaMessage: GATriggeredEvent => gaMessage.buildTypeId == configuration.frameworkLeafArtifactBuildTypeId
      case _ => false
    }
  }

  @Bean
  def synchronizedDependencyUpdateHandler(producers: Producers, resilientMaker: ResilientProducerMaker,
                                          artifactAwareCacheFolder: CacheFolder): DependencyUpdateHandler = {
    initTopics()
    val managedDepsProducer = resilientMaker.withTopic(synchronizedManagedDepsTopic).ordered.build
    producers.add(managedDepsProducer)

    val fwLeafProducer = resilientMaker.withTopic(synchronizedFrameworkLeafTopic).ordered.build
    producers.add(fwLeafProducer)

    val managedDepsBazelRepository: BazelRepository = {
      val checkoutDirectory = File(artifactAwareCacheFolder.folder.getAbsolutePath) / "managed_deps_clone"
      val authenticationWithToken = new GitAuthenticationWithToken(Option(configuration.git.githubToken).filterNot(_.isEmpty))

      new GitBazelRepository(
        configuration.git.managedDepsRepoURL,
        checkoutDirectory,
        configuration.git.username,
        configuration.git.email
      )(authenticationWithToken)
    }

    val serverInfraBazelRepository: BazelRepository = {
      val checkoutDirectory = File(artifactAwareCacheFolder.folder.getAbsolutePath) / "fw_ga_clone"
      val authenticationWithToken = new GitAuthenticationWithToken(Option(configuration.git.githubToken).filterNot(_.isEmpty))

      new GitBazelRepository(
        configuration.git.serverInfraRepoURL,
        checkoutDirectory,
        configuration.git.username,
        configuration.git.email
      )(authenticationWithToken)
    }

    val storage = new StaticDependenciesRemoteStorage(new ArtifactoryRemoteStorage(configuration.artifactoryUrl, configuration.artifactoryToken))

    val managedDependenciesUpdateHandler = new ManagedDependenciesUpdateHandler(dependencyManagementArtifact,
      managedDepsBazelRepository,
      configuration.mavenRemoteRepositoryURL,
      storage
    )

    val  frameworkGAUpdateHandler= new FrameworkGAUpdateHandler(serverInfraBazelRepository,
      managedDepsBazelRepository,
      dependencyManagementArtifact,
      configuration.mavenRemoteRepositoryURL,
      storage,
      configuration.frameworkLeafArtifact,
      resolveBranchSuffix
    )


    new DependencyUpdateHandler(
      managedDependenciesUpdateHandler,
      frameworkGAUpdateHandler,
      managedDepsProducer,
      fwLeafProducer)
  }

  private def resolveBranchSuffix = {
    if (configuration.branchSuffix.isEmpty)
      UUID.randomUUID().toString
    else
      configuration.branchSuffix
  }

  @Autowired
  def setConsumers(consumers: Consumers,
                   dependencyUpdateHandler: DependencyUpdateHandler): Unit = {
    initTopics()

    setManagedDepsSyncMessagesConsumers(consumers, dependencyUpdateHandler)
    setFrameworkLeafSyncConsumers(consumers, dependencyUpdateHandler)
  }

  private def setFrameworkLeafSyncConsumers(consumers: Consumers, dependencyUpdateHandler: DependencyUpdateHandler) = {
    val gaMessageHandler = MessageHandler
      .aMessageHandler[BasePromote](dependencyUpdateHandler.handleGAMessage)
      .withMapper(JsonMapper.global)
      .withFilter(fwGaTriggeredMessage)
      .build
    val gaMessageConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(gaTopic, gaMessageHandler)
      .withGroup("bazel-ga")

    val synchronizeFrameworkLeafHandler = MessageHandler
      .aMessageHandler[GATriggeredEvent](dependencyUpdateHandler.handleMessageFromSynchronizedFrameworkLeafTopic)
      .withMapper(JsonMapper.global)
      .build
    val synchronizeFrameworkLeafMessageConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(synchronizedFrameworkLeafTopic, synchronizeFrameworkLeafHandler)
      .withGroup("fw-leaf-synchronizer")
      .withMaxParallelism(1)

    consumers.add(gaMessageConsumer)
    consumers.add(synchronizeFrameworkLeafMessageConsumer)
  }

  private def initTopics(): Unit = {
    val kafkaAdmin = new KafkaGreyhoundAdmin()
    kafkaAdmin.createTopicIfNotExists(buildTopic)
    kafkaAdmin.createTopicIfNotExists(gaTopic)
    kafkaAdmin.createTopicIfNotExists(synchronizedManagedDepsTopic, partitions = 1)
    kafkaAdmin.createTopicIfNotExists(synchronizedFrameworkLeafTopic, partitions = 1)
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

    val synchronizeManagedDepsHandler = MessageHandler
      .aMessageHandler[BuildFinished](dependencyUpdateHandler.handleMessageFromSynchronizedManagedDepsTopic)
      .withMapper(JsonMapper.global)
      .build
    val synchronizeManagedDepsMessageConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(synchronizedManagedDepsTopic, synchronizeManagedDepsHandler)
      .withGroup("managed-deps-synchronizer")
      .withMaxParallelism(1)
    consumers.add(buildMessageConsumer)
    consumers.add(synchronizeManagedDepsMessageConsumer)
  }
}
