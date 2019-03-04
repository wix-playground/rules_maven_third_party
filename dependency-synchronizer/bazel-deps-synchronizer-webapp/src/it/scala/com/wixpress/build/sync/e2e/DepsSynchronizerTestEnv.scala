package com.wix.build.sync.e2e

import com.wix.bootstrap.BootstrapManagedService
import com.wix.e2e.PortRandomizer
import com.wix.e2e.http.Implicits.DefaultBaseUri
import com.wix.build.maven.{Coordinates, FakeMavenRepository, Packaging}
import com.wix.build.sync.{BazelMavenSynchronizerConfig, BazelMavenSynchronizerServer}
import com.wix.ci.greyhound.events.{Lifecycle, TeamcityTopic}
import com.wix.framework.petri.PetriTestkit
import com.wix.framework.test.env.{Configurer, TestEnv, TestEnvBuilder}
import com.wix.greyhound.KafkaManagedService
import com.wix.hoopoe.config.TestConfigFactory
import com.wix.musterguard.drivers.MasterGuardTestEnvSupport

object DepsSynchronizerTestEnv extends MasterGuardTestEnvSupport {
  private def mavenRepoPort = PortRandomizer.selectRandomPort()

  val fakeMavenRepository = new FakeMavenRepository(mavenRepoPort)
  val wiremock = new WireMockManagedService
  wiremock.alwaysReturnSha256Checksums()

  val newBranchSuffix = "someSuffix"
  val userAddedDepsBranchName = s"user_added_3rd_party_deps_$newBranchSuffix"
  val dependencyManagerArtifact: Coordinates = Coordinates("some-group", "third-party", "some-version", packaging = Packaging("pom"))
  val fakeManagedDepsRemoteRepository: FakeRemoteRepository = FakeRemoteRepository.newBlankRepository()
  private val kafka = KafkaManagedService(TeamcityTopic.TeamcityEvents, Lifecycle.lifecycleGaTopic)
  private val mainService = BootstrapManagedService(BazelMavenSynchronizerServer)
  val gitUsername = "builduser"
  val gitUserEmail = "builduser@ci.com"
  val thirdPartySyncbuildTypeID = "100x"

  object E2EConfigurer extends Configurer {


    override def configureEnvironment(): Unit = {
      val mutator: BazelMavenSynchronizerConfig => BazelMavenSynchronizerConfig = (config: BazelMavenSynchronizerConfig) => {
        config.copy(
          dependencyManagementArtifact = dependencyManagerArtifact.serialized,
          dependencyManagementArtifactBuildTypeId = thirdPartySyncbuildTypeID,
          mavenRemoteRepositoryURL = List(fakeMavenRepository.url),
          artifactoryUrl = s"localhost:${WireMockTestSupport.wireMockPort}",
          git = config.git.copy(
            managedDepsRepoURL = fakeManagedDepsRemoteRepository.remoteURI,
            username = gitUsername,
            email = gitUserEmail
          ),
          branchSuffix = newBranchSuffix
        )
      }
      TestConfigFactory.aTestEnvironmentFor[BazelMavenSynchronizerConfig](
        "bazel-deps-synchronizer", mutator,
        "databag_passwd.com.wix.build.bazel-deps-synchronizer.github_token" -> "",
        "databag_passwd.com.wix.build.bazel-deps-synchronizer.artifactory_token" -> ""
      )
    }
  }

  private val petri = PetriTestkit().withFreshFakeServer().build

  def env: TestEnv = TestEnvBuilder()
    .withCollaborators(kafka, fakeMavenRepository, petri, wiremock, masterGuardRpcServer )
    .withMainServiceConfigurer(E2EConfigurer)
    .withMainService(mainService)
    .build()
}
