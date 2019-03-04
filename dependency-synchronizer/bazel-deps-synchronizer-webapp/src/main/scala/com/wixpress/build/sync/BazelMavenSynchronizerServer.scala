package com.wix.build.sync

import java.util.UUID

import better.files.File
import com.wix.bootstrap.jetty.BootstrapServer
import com.wix.build.sync.api.BazelSyncGreyhoundEvents
import com.wix.build.bazel.BazelRepository
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
import com.wix.vi.githubtools.masterguard.enforceadmins.MasterEnforcer
import com.wix.vi.githubtools.masterguard.spring.EnforceAdminsSpringConfig


object BazelMavenSynchronizerServer extends BootstrapServer {
  override def additionalSpringConfig = Some(classOf[SynchronizerConfiguration])
}

@Configuration
@Import(Array(classOf[JsonRpcServerConfiguration], classOf[GreyhoundSpringConfig], classOf[CacheFolderConfig], classOf[EnforceAdminsSpringConfig]))
class SynchronizerConfiguration {
  
  private val configuration = BazelMavenSynchronizerConfig.root

  private val buildTopic = TeamcityTopic.TeamcityEvents

  private val dependencyManagementArtifact: Coordinates = Coordinates.deserialize(configuration.dependencyManagementArtifact)

  private val synchronizedManagedDepsTopic = s"${buildTopic}_${dependencyManagementArtifact.serialized.replace(":", "_")}"

  private def buildFinishedMessage= (buildFinished: BuildFinished) => {
    buildFinished.isSuccessful &&
      buildFinished.buildConfigId == configuration.dependencyManagementArtifactBuildTypeId
  }

  @Bean
  def synchronizedDependencyUpdateHandler(producers: Producers, resilientMaker: ResilientProducerMaker,
                                          artifactAwareCacheFolder: CacheFolder,
                                          masterEnforcer: MasterEnforcer): DependencyUpdateHandler = {
    initTopics()
    val managedDepsProducer = resilientMaker.withTopic(synchronizedManagedDepsTopic).ordered.build
    producers.add(managedDepsProducer)

    val syncEndedProducer = resilientMaker.withTopic(BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic).unordered.build
    producers.add(syncEndedProducer)

    val managedDepsBazelRepository: BazelRepository = {
      val checkoutDirectory = File(artifactAwareCacheFolder.folder.getAbsolutePath) / "managed_deps_clone"
      val authenticationWithToken = new GitAuthenticationWithToken(Option(configuration.git.githubToken).filterNot(_.isEmpty))

      new GitBazelRepository(
        configuration.git.managedDepsRepoURL,
        checkoutDirectory,
        masterEnforcer,
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

    val managedDepsSyncFinished = new ManagedDepsSyncFinished(managedDepsBazelRepository,syncEndedProducer)

    new DependencyUpdateHandler(
      managedDependenciesUpdateHandler,
      managedDepsProducer,
      managedDepsBazelRepository,
      managedDepsSyncFinished)
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
  }

  private def initTopics(): Unit = {
    val kafkaAdmin = new KafkaGreyhoundAdmin()
    kafkaAdmin.createTopicIfNotExists(buildTopic)
    kafkaAdmin.createTopicIfNotExists(synchronizedManagedDepsTopic, partitions = 1)
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
