package com.wix.build.sync.e2e

import com.wix.build.sync.api.{BazelManagedDepsSyncEnded, BazelSyncGreyhoundEvents, ThirdPartyArtifact}
import com.wix.e2e.http.Implicits.DefaultBaseUri
import com.wix.e2e.http.client.sync.get
import com.wix.build.maven.ArtifactDescriptor._
import com.wix.build.maven.{Coordinates, Dependency, MavenScope, Packaging}
import com.wix.build.sync.e2e.DepsSynchronizerTestEnv._
import com.wix.ci.greyhound.events.{BuildFinished, TeamcityTopic, VcsUpdate}
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
  private val someOtherCoordinates = Coordinates("com.wix.example2", "some-artifact2", "someVersion2")

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

  def manuallyCallDepsSync(branchName: String) = {
    get(s"api/sync?branch=$branchName")
  }

  def produceMessageAboutCommitToManagedBzlDeps(): Unit = {
    val producer = ProducerMaker.aProducer().buffered.ordered.build
    val vcsUpdateMessage = VcsUpdate(
      url = DepsSynchronizerTestEnv.fakeManagedDepsRemoteRepository.remoteURI,
      branch = "master",
      revision = "dont-care",
      oldRevision = Some("dont-care"))

    producer.produceToTopic(VcsUpdate.greyhoundTopic, vcsUpdateMessage)
  }

  "Bazel-Maven Deps Synchronizer," >> {

    "add dependency from pom to target bazel repository and push to source control" should {
      "when notification about pom build received" in new ctx {
        manage(someCoordinates)

        produceMessageAboutManagedDepsChange()

        eventually {
          fakeManagedDepsRemoteRepository must haveWorkspaceRuleFor(someCoordinates, buildRunId)
        }
      }
      "when manually called" in new ctx {
        manage(someOtherCoordinates)

        val requestedBranchName = "blah"
        manuallyCallDepsSync(branchName = requestedBranchName)

        eventually {
          fakeManagedDepsRemoteRepository must haveWorkspaceRuleFor(someOtherCoordinates, requestedBranchName)
        }
      }
    }

    "when notification about managed deps master change received" should {
      "notify of bazel managed deps" in new ctx {

        fakeManagedDepsRemoteRepository.commitThirdParties(Map(
          "first_group.bzl" -> importExternal1,
          "other_group.bzl" -> importExternal2))

        produceMessageAboutCommitToManagedBzlDeps()

        eventually {
          sink.getMessages.size must be_===(1)
          sink.getMessages.head.thirdPartyArtifacts must containAllOf(expectedThirdPartyArtifacts)
        }
      }
    }
  }

  def importExternal1 =
    s"""
       |load("@core_server_build_tools//:import_external.bzl", import_external = "safe_wix_scala_maven_import_external")
       |
       |def dependencies():
       |
       |  import_external(
       |      name = "first_group_some_artifact",
       |      artifact = "first.group:some-artifact:someVersion"
       |  )
       |
     """.stripMargin

  def importExternal2 =
    s"""
       |load("@core_server_build_tools//:import_external.bzl", import_external = "safe_wix_scala_maven_import_external")
       |
       |def dependencies():
       |
       |  import_external(
       |      name = "other_group_some_artifact",
       |      artifact = "other.group:some-artifact:someVersion"
       |  )
       |
     """.stripMargin

  def haveWorkspaceRuleFor(someCoordinates: Coordinates, branchName: String): Matcher[FakeRemoteRepository] =
    beSuccessfulTry ^^ ((_: FakeRemoteRepository).hasWorkspaceRuleFor(someCoordinates, branchName))

  trait ctx extends Scope {
    val sink = anEventSink[BazelManagedDepsSyncEnded](BazelSyncGreyhoundEvents.BazelManagedDepsSyncEndedTopic)
    val expectedThirdPartyArtifacts = Seq(
      ThirdPartyArtifact("other.group", "some-artifact", "someVersion", Packaging("jar").value, None, None),
      ThirdPartyArtifact("first.group", "some-artifact", "someVersion", Packaging("jar").value, None, None)
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

