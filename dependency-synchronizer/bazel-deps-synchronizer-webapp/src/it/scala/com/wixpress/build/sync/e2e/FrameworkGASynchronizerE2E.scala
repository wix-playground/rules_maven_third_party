package com.wix.build.sync.e2e

import com.wix.build.maven.ArtifactDescriptor._
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven._
import com.wix.build.sync.e2e.DepsSynchronizerTestEnv._
import com.wix.ci.buildserver.BuildServerId
import com.wix.ci.greyhound.events.{GATriggeredEvent, Lifecycle}
import com.wix.framework.test.env.{GlobalTestEnvSupport, TestEnv}
import com.wix.greyhound.GreyhoundTestingSupport
import com.wix.greyhound.producer.builder.ProducerMaker
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class FrameworkGASynchronizerE2E extends SpecificationWithJUnit with GreyhoundTestingSupport with GlobalTestEnvSupport {
  sequential

  val newVersion = "new-version"

  private val someFWModuleCoordinates = Coordinates("com.wix.example", "some-fw-artifact", "someVersion")

  def produceMessageAbout(currentVersion: Option[String] = None): Unit = {
    val producer = ProducerMaker.aProducer().buffered.ordered.build
    val promotedGaMessage = GATriggeredEvent(
      buildName = "dont-care",
      buildTypeId = DepsSynchronizerTestEnv.fwSyncbuildTypeID,
      buildServerId = BuildServerId.JVM_CI,
      version = currentVersion.fold("dont-care")(v => v),
      comment = "dont-care",
      user = "dont-care",
      email = "dont-care"
    )

    producer.produceToTopic(Lifecycle.lifecycleGaTopic, promotedGaMessage)
  }

  "Bazel-Maven Deps Synchronizer," >> {
    "given pom, with new dependency X, was updated in dependency source," >> {
      "when notification was received about it," should {
        "add dependency X to target bazel repository and push to source control" in new ctx {
          addDep(someFWModuleCoordinates, fwLeafCoordinates.copy(version = newVersion + "-SNAPSHOT"))

          produceMessageAbout(Some(newVersion))

          eventually {
            fakeServerInfraRemoteRepository must haveWorkspaceRuleFor(someFWModuleCoordinates, userAddedDepsBranchName)
          }
        }
      }
    }
  }

  def haveWorkspaceRuleFor(someCoordinates: Coordinates, forBranch: String): Matcher[FakeRemoteRepository] =
    beSuccessfulTry ^^ ((_: FakeRemoteRepository).hasWorkspaceRuleFor(someCoordinates, forBranch))

  trait ctx extends Scope {
    val baseDepsManagementArtifact = anArtifact(
      coordinates = dependencyManagerArtifact
    )

    fakeMavenRepository.addArtifacts(baseDepsManagementArtifact)


    def addDep(coordinates: Coordinates, frameworkLeafArtifact: Coordinates) = {
      val dependantDescriptor = ArtifactDescriptor.withSingleDependency(frameworkLeafArtifact, asCompileDependency(coordinates))
      val dependencyDescriptor = ArtifactDescriptor.rootFor(coordinates)

      fakeMavenRepository.addArtifacts(dependantDescriptor, dependencyDescriptor)
    }
  }

  override def testEnv: TestEnv = DepsSynchronizerTestEnv.env
}

