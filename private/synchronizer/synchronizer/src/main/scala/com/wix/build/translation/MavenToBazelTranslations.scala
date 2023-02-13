package com.wix.build.translation

import com.wix.build.maven.Coordinates

object MavenToBazelTranslations {
  implicit class `Maven Coordinates to Bazel rules`(coordinates: Coordinates) {
    import coordinates._

    def groupIdForBazel: String = {
      fixNameToBazelConventions(groupId)
    }

    def workspaceRuleName: String = {
      val groupIdPart = fixNameToBazelConventions(groupId)
      s"${groupIdPart}_$libraryRuleName"
    }

    def libraryRuleName: String = {
      val artifactIdPart = fixNameToBazelConventions(artifactId)
      val classifierPart = classifier.map(c => s"_${fixNameToBazelConventions(c)}").getOrElse("")
      s"$artifactIdPart$classifierPart"
    }

    def workspaceRuleNameVersioned: String = {
      s"${workspaceRuleName}_${fixNameToBazelConventions(version)}"
    }

    private def fixNameToBazelConventions(id: String): String = {
      id.replace('-', '_').replace('.', '_')
    }
  }
}
