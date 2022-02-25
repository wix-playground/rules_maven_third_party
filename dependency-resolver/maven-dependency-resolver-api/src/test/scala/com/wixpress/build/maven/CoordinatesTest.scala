package com.wix.build.maven

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class CoordinatesTest extends SpecificationWithJUnit {

  trait baseCtx extends Scope {
    val someGroupId = "some.group"
    val someArtifactId = "some-artifact"
    val someVersion = "someVersion"
    val packaging = "pack"
    val classifier = "class"
    val baseCoordinates = Coordinates(
      groupId = someGroupId,
      artifactId = someArtifactId,
      version = someVersion
    )
  }


  trait equalityCtx extends baseCtx {

    implicit class extendedCoordinates(coordinates: Coordinates) {
      private def otherOption(optionalString: Option[String]) = Some(optionalString.map(_ + "-other").getOrElse("other"))

      def withDifferentGroupId = coordinates.copy(groupId = coordinates.groupId + "-other")

      def withDifferentArtifactId = coordinates.copy(artifactId = coordinates.artifactId + "-other")

      def withDifferentVersion = coordinates.copy(version = coordinates.version + "-other")

      def withDifferentClassifier = coordinates.copy(classifier = otherOption(coordinates.classifier))

      def withDifferentPackaging = coordinates.copy(packaging = otherOption(coordinates.packaging))
    }

    val coordinatesWithDifferentVersionPackagingAndClassifier = baseCoordinates.copy(
      version = "other-version",
      packaging = Some("other-packaging"),
      classifier = Some("other-classifier")
    )

    val coordinatesWithDifferentPackagingAndClassifier = baseCoordinates.copy(
      version = "other-version",
      packaging = Some("other-packaging"),
      classifier = Some("other-classifier")
    )

    val coordinatesWithDifferentGroupId = baseCoordinates.copy(groupId = "other.group")
    val coordinatesWithDifferentArtifactId = baseCoordinates.copy(artifactId = "other-artifact")
  }

  "Coordinates" should {


    "return the name of matching scala library target to given maven coordinates" in new baseCtx {
      val expectedName = baseCoordinates.artifactId.replace('-', '_').replace('.', '_')

      baseCoordinates.libraryRuleName mustEqual expectedName
    }

    "return the name of matching scala library target to given maven coordinates with classifier" in new baseCtx {
      val someClassifier = "some-classifier"
      val coordinatesWithClassifier = baseCoordinates.copy(classifier = Some(someClassifier))

      coordinatesWithClassifier.libraryRuleName mustEqual "some_artifact_some_classifier"
    }

    "return the name of the matching workspace rule name for given maven coordinates" in new baseCtx {
      baseCoordinates.workspaceRuleName mustEqual "some_group_some_artifact"
    }

    "return the name of the matching workspace rule name for given maven coordinates with classifier" in new baseCtx {
      val someClassifier = "some-classifier"
      val coordinatesWithClassifier = baseCoordinates.copy(classifier = Some(someClassifier))

      coordinatesWithClassifier.workspaceRuleName mustEqual "some_group_some_artifact_some_classifier"
    }

    "set packaging to jar when not defined" in new baseCtx {
      val coordinates = Coordinates("group", "artifact", "version")

      coordinates.packaging must beSome("jar")
    }

    "serialize (groupId,artifactId,version) Coordinates to colon string" in new baseCtx {
      val expectedColonRepresentation =
        s"${baseCoordinates.groupId}:${baseCoordinates.artifactId}:${baseCoordinates.version}"
      baseCoordinates.serialized mustEqual expectedColonRepresentation
    }

    "serialize (groupId,artifactId,packaging,version) Coordinates to colon string, given packaging is not jar" in new baseCtx {
      val extendedCoordinates = baseCoordinates.copy(packaging = Some(packaging))

      val expectedColonRepresentation =
        s"${extendedCoordinates.groupId}:${extendedCoordinates.artifactId}:$packaging:${extendedCoordinates.version}"
      extendedCoordinates.serialized mustEqual expectedColonRepresentation
    }

    "serialize (groupId,artifactId,packaging,classifier,version) Coordinates to colon string, even if packaging is jar" in new baseCtx {
      val jarPackaging = "jar"
      val extendedCoordinates = baseCoordinates.copy(packaging = Some(jarPackaging), classifier = Some(classifier))
      val expectedColonRepresentation =
        s"${extendedCoordinates.groupId}:${extendedCoordinates.artifactId}:$jarPackaging:$classifier:${extendedCoordinates.version}"

      extendedCoordinates.serialized mustEqual expectedColonRepresentation
    }

    "serialize (groupId,artifactId,packaging,classifier,version) Coordinates to colon string" in new baseCtx {
      val extendedCoordinates = baseCoordinates.copy(packaging = Some(packaging), classifier = Some(classifier))
      val expectedColonRepresentation =
        s"${extendedCoordinates.groupId}:${extendedCoordinates.artifactId}:$packaging:$classifier:${extendedCoordinates.version}"

      extendedCoordinates.serialized mustEqual expectedColonRepresentation
    }

    "deserialize from 3 part colon string to Coordinates" in new baseCtx {
      val colonString = s"$someGroupId:$someArtifactId:$someVersion"
      Coordinates.deserialize(colonString) mustEqual baseCoordinates
    }

    "deserialize from 4 part colon string to Coordinates" in new baseCtx {
      val colonString = s"$someGroupId:$someArtifactId:$packaging:$someVersion"
      val extendedCoordinates = baseCoordinates.copy(packaging = Some(packaging))

      Coordinates.deserialize(colonString) mustEqual extendedCoordinates
    }

    "deserialize from 5 part colon string to Coordinates" in new baseCtx {
      val colonString = s"$someGroupId:$someArtifactId:$packaging:$classifier:$someVersion"
      val extendedCoordinates = baseCoordinates.copy(packaging = Some(packaging), classifier = Some(classifier))

      Coordinates.deserialize(colonString) mustEqual extendedCoordinates
    }

    "convert to notification port topic" in new baseCtx {
      val topic = baseCoordinates.asTopic

      topic mustEqual baseCoordinates.serialized.replace(":", "_")
    }

    "convert to aether artifact" in new baseCtx {
      val aetherArtifact = baseCoordinates.asAetherArtifact

      aetherArtifact.getGroupId mustEqual baseCoordinates.groupId
      aetherArtifact.getArtifactId mustEqual baseCoordinates.artifactId
      aetherArtifact.getVersion mustEqual baseCoordinates.version
      aetherArtifact.getClassifier mustEqual baseCoordinates.classifier.getOrElse("")
      aetherArtifact.getExtension mustEqual baseCoordinates.packaging.get
    }

    "check equality based on groupId and artifactId" in new equalityCtx {
      baseCoordinates.equalsOnGroupIdAndArtifactId(baseCoordinates.withDifferentGroupId) must beFalse
      baseCoordinates.equalsOnGroupIdAndArtifactId(baseCoordinates.withDifferentArtifactId) must beFalse
      baseCoordinates.equalsOnGroupIdAndArtifactId(baseCoordinates.withDifferentVersion) must beTrue
      baseCoordinates.equalsOnGroupIdAndArtifactId(baseCoordinates.withDifferentClassifier) must beTrue
      baseCoordinates.equalsOnGroupIdAndArtifactId(baseCoordinates.withDifferentPackaging) must beTrue
    }

    "check equality ignoring version" in new equalityCtx {
      baseCoordinates.equalsIgnoringVersion(baseCoordinates.withDifferentGroupId) must beFalse
      baseCoordinates.equalsIgnoringVersion(baseCoordinates.withDifferentArtifactId) must beFalse
      baseCoordinates.equalsIgnoringVersion(baseCoordinates.withDifferentVersion) must beTrue
      baseCoordinates.equalsIgnoringVersion(baseCoordinates.withDifferentClassifier) must beFalse
      baseCoordinates.equalsIgnoringVersion(baseCoordinates.withDifferentPackaging) must beFalse
    }

  }
}
