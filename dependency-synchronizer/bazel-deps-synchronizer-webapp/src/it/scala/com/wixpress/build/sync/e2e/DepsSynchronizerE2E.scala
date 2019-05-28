package com.wix.build.sync.e2e

import com.wix.build.sync.api.{BazelManagedDepsSyncEnded, BazelSyncGreyhoundEvents, ThirdPartyArtifact}
import com.wix.build.maven.ArtifactDescriptor._
import com.wix.build.maven.{Coordinates, Dependency, MavenScope, Packaging}
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

  private val buildRunId = "someStringOfVersion"

  def produceMessageAboutManagedDepsChange(): Unit = {
    val producer = ProducerMaker.aProducer().buffered.ordered.build
    val buildFinishedMessage = BuildFinished(
      buildRunId = buildRunId,
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

          produceMessageAboutManagedDepsChange()

          eventually {
            fakeManagedDepsRemoteRepository must haveWorkspaceRuleFor(someCoordinates)
          }
        }
      }
    }

    //sink.getMessages must contain(BazelManagedDepsSyncEnded(expectedThirdPartyArtifact))

  }

  def haveWorkspaceRuleFor(someCoordinates: Coordinates): Matcher[FakeRemoteRepository] =
    beSuccessfulTry ^^ ((_: FakeRemoteRepository).hasWorkspaceRuleFor(someCoordinates, branchName = buildRunId))

  trait ctx extends Scope {
    val sink = anEventSink[BazelManagedDepsSyncEnded](BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic)
    //TODO - unused...
    val expectedThirdPartyArtifact = Set(
      ThirdPartyArtifact("other-group","some-artifact","someVersion",Packaging("jar").value,None,None),
      ThirdPartyArtifact("com.wix.example","some-artifact","someVersion",Packaging("jar").value,None,None)
    )

    def manage(coordinates: Coordinates) = {
      val artifact = anArtifact(coordinates)
      val artifactAsDependency = Dependency(coordinates, MavenScope.Compile)
      val updatedDependencyManagementArtifact = baseDepsManagementArtifact.withManagedDependency(artifactAsDependency)
      fakeMavenRepository.addArtifacts(artifact, updatedDependencyManagementArtifact)
    }
  }

  override def testEnv: TestEnv = DepsSynchronizerTestEnv.env
}

