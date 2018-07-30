package com.wix.build.sync.e2e

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

  case class UpdateMessage(artifact: String, timestamp: Long)

  private val baseDepsManagementArtifact = anArtifact(
    coordinates = dependencyManagerArtifact
  )

  private val someCoordinates = Coordinates("com.wix.example", "some-artifact", "someVersion")
  private val otherCoordinates: Coordinates = someCoordinates.copy(groupId = "other-group")

  private def produceMessageAbout(coordinates: Coordinates): Unit = {
    val producer = ProducerMaker.aProducer().buffered.ordered.build
    val buildFinishedMessage = BuildFinished(
      buildRunId = "dont-care",
      buildConfigId = DepsSynchronizerTestEnv.buildTypeID,
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
          val updatedDependencyManagementArtifact = manage(someCoordinates)

          produceMessageAbout(dependencyManagerArtifact)


          eventually {
            fakeRemoteRepository must haveWorkspaceRuleFor(someCoordinates)
          }
        }
      }
    }
    "given pom, with new dependency X, was updated in dependency source and later was also update with new dependency Y," >> {
      "after two notifications were received about these updates," should {

        "add both dependency X and dependency Y to target bazel repository" in new ctx {
          val updatedDependencyManagementArtifact = manage(someCoordinates)

          produceMessageAbout(dependencyManagerArtifact)

          manage(otherCoordinates)

          produceMessageAbout(dependencyManagerArtifact)

          eventually {
            fakeRemoteRepository must haveWorkspaceRuleFor(someCoordinates) and haveWorkspaceRuleFor(otherCoordinates)
          }
        }
      }
    }
  }

  def haveWorkspaceRuleFor(someCoordinates: Coordinates): Matcher[FakeRemoteRepository] =
    beSuccessfulTry ^^ ((_: FakeRemoteRepository).hasWorkspaceRuleFor(someCoordinates))

  trait ctx extends Scope {
    def manage(coordinates: Coordinates) = {
      val artifact = anArtifact(coordinates)
      val artifactAsDependency = Dependency(coordinates, MavenScope.Compile)
      val updatedDependencyManagementArtifact = baseDepsManagementArtifact.withManagedDependency(artifactAsDependency)
      fakeMavenRepository.addArtifacts(artifact, updatedDependencyManagementArtifact)
      updatedDependencyManagementArtifact
    }
  }

  override def testEnv: TestEnv = DepsSynchronizerTestEnv.env
}

