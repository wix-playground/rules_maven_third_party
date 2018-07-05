package com.wix.build.sync.e2e

import com.wix.bootstrap.BootstrapManagedService
import com.wix.e2e.PortRandomizer
import com.wix.build.maven.{Coordinates, FakeMavenRepository, Packaging}
import com.wix.build.sync.{BazelMavenSynchronizerConfig, BazelMavenSynchronizerServer}
import com.wix.ci.greyhound.events.TeamcityTopic
import com.wix.framework.petri.PetriTestkit
import com.wix.framework.test.env.{Configurer, TestEnv, TestEnvBuilder}
import com.wix.greyhound.KafkaManagedService
import com.wix.hoopoe.config.TestConfigFactory
import com.wix.e2e.http.Implicits.DefaultBaseUri

object DepsSynchronizerTestEnv {
  private def mavenRepoPort = PortRandomizer.selectRandomPort()

  val fakeMavenRepository = new FakeMavenRepository(mavenRepoPort)

  val dependencyManagerArtifact: Coordinates = Coordinates("some-group", "third-party", "some-version", packaging = Packaging("pom"))
  val fakeRemoteRepository: FakeRemoteRepository = FakeRemoteRepository.newBlankRepository
  private val kafka = KafkaManagedService(TeamcityTopic.TeamcityEvents)
  private val mainService = BootstrapManagedService(BazelMavenSynchronizerServer)
  val gitUsername = "builduser"
  val gitUserEmail = "builduser@ci.com"
  val buildTypeID = "100x"

  object E2EConfigurer extends Configurer {


    override def configureEnvironment(): Unit = {
      val mutator: (BazelMavenSynchronizerConfig) => BazelMavenSynchronizerConfig = (config: BazelMavenSynchronizerConfig) => {
        config.copy(
          dependencyManagementArtifact = dependencyManagerArtifact.serialized,
          dependencyManagementArtifactBuildTypeId = buildTypeID,
          mavenRemoteRepositoryURL = List(fakeMavenRepository.url),
          git = config.git.copy(
            targetRepoURL = fakeRemoteRepository.remoteURI,
            username = gitUsername,
            email = gitUserEmail
          )
        )
      }
      TestConfigFactory.aTestEnvironmentFor[BazelMavenSynchronizerConfig](
        "bazel-deps-synchronizer", mutator,
        "databag_passwd.com.wix.build.bazel-deps-synchronizer.github_token" -> ""
      )
    }
  }

  private val petri = PetriTestkit().withFreshFakeServer().build

  def env: TestEnv = TestEnvBuilder()
    .withEnvironmentConfigurer(E2EConfigurer)
    .withCollaborators(kafka, fakeMavenRepository, petri)
    .withMainService(mainService)
    .build()
}
