package com.wix.build.sync.e2e

import com.wix.build.sync.api.{BazelSyncGreyhoundEvents, BazelManagedDepsSyncEnded}
import com.wix.build.maven.ArtifactDescriptor._
import com.wix.build.maven.{Coordinates, Dependency, MavenScope}
import com.wix.build.sync.e2e.DepsSynchronizerTestEnv._
import com.wix.ci.greyhound.events.{BuildFinished, TeamcityTopic}
import com.wix.framework.test.env.{GlobalTestEnvSupport, TestEnv}
import com.wix.greyhound.GreyhoundTestingSupport
import com.wix.greyhound.producer.builder.ProducerMaker
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class DepsSynchronizerE2E extends SpecificationWithJUnit with GreyhoundTestingSupport with GlobalTestEnvSupport {
  sequential

  private val baseDepsManagementArtifact = anArtifact(
    coordinates = dependencyManagerArtifact
  )

  private val someCoordinates = Coordinates("com.wix.example", "some-artifact", "someVersion")
  private val otherCoordinates: Coordinates = someCoordinates.copy(groupId = "other-group")

  def produceMessageAboutManagedDepsChange(coordinates: Coordinates): Unit = {
    val producer = ProducerMaker.aProducer().buffered.ordered.build
    val buildFinishedMessage = BuildFinished(
      buildRunId = "dont-care",
      buildConfigId = DepsSynchronizerTestEnv.thirdPartySyncbuildTypeID,
      buildServerId = "dont-care",
      version = "dont-care",
      isSuccessful = true
    )

    producer.produceToTopic(TeamcityTopic.TeamcityEvents, buildFinishedMessage)
  }

  "Bazel-Maven Deps Synchronizer," >> {
    "given pom, with new dependency X, was updated in dependency source," >> {
      "when notification was received about it," should {
        "add dependency X to target bazel repository and push to source control" in new ctx {
          manage(someCoordinates)

          produceMessageAboutManagedDepsChange(dependencyManagerArtifact)


          eventually {
            fakeManagedDepsRemoteRepository must haveWorkspaceRuleFor(someCoordinates)
          }
        }
      }
    }
    "given pom, with new dependency X, was updated in dependency source and later was also update with new dependency Y," >> {
      "after two notifications were received about these updates," should {

        "add both dependency X and dependency Y to target bazel repository" in new ctx {
          val updatedDependencyManagementArtifact = manage(someCoordinates)

          produceMessageAboutManagedDepsChange(dependencyManagerArtifact)

          manage(otherCoordinates)

          produceMessageAboutManagedDepsChange(dependencyManagerArtifact)

          eventually {
            fakeManagedDepsRemoteRepository must haveWorkspaceRuleFor(someCoordinates) and haveWorkspaceRuleFor(otherCoordinates)
            sink.getMessages must contain(BazelManagedDepsSyncEnded())

          }
        }
      }
    }
  }

  def haveWorkspaceRuleFor(someCoordinates: Coordinates): Matcher[FakeRemoteRepository] =
    beSuccessfulTry ^^ ((_: FakeRemoteRepository).hasWorkspaceRuleFor(someCoordinates))

  trait ctx extends Scope {
    val sink = anEventSink[BazelManagedDepsSyncEnded](BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic)

    def manage(coordinates: Coordinates) = {
      val artifact = anArtifact(coordinates)
      val artifactAsDependency = Dependency(coordinates, MavenScope.Compile)
      val updatedDependencyManagementArtifact = baseDepsManagementArtifact.withManagedDependency(artifactAsDependency)
      fakeMavenRepository.addArtifacts(artifact, updatedDependencyManagementArtifact)
    }
  }

  override def testEnv: TestEnv = DepsSynchronizerTestEnv.env
}

