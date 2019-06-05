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

  private val commitUpdateTopic = VcsUpdate.greyhoundTopic

  private val dependencyManagementArtifact: Coordinates = Coordinates.deserialize(configuration.dependencyManagementArtifact)

  private val MasterBranch = "master"

  @Bean
  def managedDependenciesUpdateHandler(masterEnforcer: MasterEnforcer): ManagedDependenciesUpdateHandler = {
    val storage = new ArtifactoryRemoteStorage(configuration.artifactoryUrl, configuration.artifactoryToken)
    new ManagedDependenciesUpdateHandler(dependencyManagementArtifact,
      configuration.mavenRemoteRepositoryURL,
      storage,
      WixLoadStatements.importExternalLoadStatement,
      configuration.git,
      masterEnforcer,
    )
  }

  @Bean
  def synchronizedDependencyUpdateHandler(managedDependenciesUpdateHandler: ManagedDependenciesUpdateHandler): DependencyUpdateHandler =
    new DependencyUpdateHandler(managedDependenciesUpdateHandler)


  @Bean
  def managedBazelDepsUpdateHandler(producers: Producers, resilientMaker: ResilientProducerMaker, masterEnforcer: MasterEnforcer): ManagedBazelDepsUpdateHandler = {
    initTopics()
    val syncEndedProducer = resilientMaker.withTopic(BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic).unordered.build
    producers.add(syncEndedProducer)

    new ManagedBazelDepsUpdateHandler(configuration.git, masterEnforcer, syncEndedProducer)
  }

  @Bean
  def manualController(managedDependenciesUpdateHandler: ManagedDependenciesUpdateHandler) =
    new ManualSyncController(managedDependenciesUpdateHandler)

  @Autowired
  def setConsumers(consumers: Consumers,
                   dependencyUpdateHandler: DependencyUpdateHandler,
                   managedBazelDepsUpdateHandler: ManagedBazelDepsUpdateHandler): Unit = {
    initTopics()

    setManagedDepsSyncMessagesConsumers(consumers, dependencyUpdateHandler, managedBazelDepsUpdateHandler)
  }

  private def initTopics(): Unit = {
    val kafkaAdmin = new KafkaGreyhoundAdmin()
    kafkaAdmin.createTopicIfNotExists(buildTopic)
    kafkaAdmin.createTopicIfNotExists(commitUpdateTopic)
    kafkaAdmin.createTopicIfNotExists(BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic, partitions = 1)

  }

  private def buildFinishedMessage = (buildFinished: BuildFinished) => {
    buildFinished.isSuccessful &&
      buildFinished.buildConfigId == configuration.dependencyManagementArtifactBuildTypeId
  }

  private def managedDepsRepoMasterMessage = (vcsUpdate: VcsUpdate) => {
    vcsUpdate.branch == MasterBranch && vcsUpdate.url == configuration.git.managedDepsRepoURLSshFormat
  }

  private def setManagedDepsSyncMessagesConsumers(consumers: Consumers,
                                                  dependencyUpdateHandler: DependencyUpdateHandler,
                                                  managedBazelDepsUpdateHandler: ManagedBazelDepsUpdateHandler): Unit = {
    val buildMessageHandler = MessageHandler
      .aMessageHandler[BuildFinished](dependencyUpdateHandler.handleBuildMessage)
      .withMapper(JsonMapper.global)
      .withFilter(buildFinishedMessage)
      .build
    val buildMessageConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(buildTopic, buildMessageHandler)
      .withGroup("bazel-build")
    consumers.add(buildMessageConsumer)

    val managedBzlDepsCommitHandler = MessageHandler
      .aMessageHandler[VcsUpdate](managedBazelDepsUpdateHandler.publishEventWithManagedBzlDeps)
      .withMapper(JsonMapper.global)
      .withFilter(managedDepsRepoMasterMessage)
      .build
    val managedBzlDepsCommitConsumer = GreyhoundConsumerSpec
      .aGreyhoundConsumerSpec(commitUpdateTopic, managedBzlDepsCommitHandler)
      .withGroup("managed-bazel-deps-notify")
    consumers.add(managedBzlDepsCommitConsumer)
  }

}
