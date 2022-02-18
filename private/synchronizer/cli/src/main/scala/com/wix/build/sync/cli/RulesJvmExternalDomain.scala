package com.wix.build.sync.cli

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.wix.build.maven._
import com.wix.build.maven.mapper.Mapper

object RulesJvmExternalDomain {
  private val mapper = Mapper.mapper

  def convertJsonStringToMavenDep(jsonString: String): Dependency = {
    val rulesJvmExternalDep = mapper.readValue(jsonString, classOf[RulesJvmExternalDependency])
    toMavenDependency(rulesJvmExternalDep)
  }

  private def toMavenDependency(d: RulesJvmExternalDependency) = {
    Dependency(
      coordinates = Coordinates(
        groupId = d.group,
        artifactId = d.artifact,
        version = d.version,
        packaging = d.packaging.map(Packaging).getOrElse(Packaging("jar")),
        classifier = d.classifier
      ),
      scope = MavenScope.Compile,
      isNeverLink = d.neverLink.contains(true),
      exclusions = d.exclusions.map(_.map(e => Exclusion(e.group, e.artifact))).getOrElse(Set.empty),
      tags = d.tags.getOrElse(Set.empty),
      aliases = d.aliases.getOrElse(Set.empty),
      flattenTransitiveDeps = d.flattenTransitiveDeps.contains(true)
    )
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private case class RulesJvmExternalDependency(group: String,
                                                artifact: String,
                                                version: String,
                                                packaging: Option[String],
                                                classifier: Option[String],
                                                exclusions: Option[Set[RulesJvmExternalExclusion]],
                                                neverLink: Option[Boolean],
                                                tags: Option[Set[String]],
                                                aliases: Option[Set[String]],
                                                flattenTransitiveDeps: Option[Boolean])

  @JsonIgnoreProperties(ignoreUnknown = true)
  private case class RulesJvmExternalExclusion(group: String, artifact: String)

}
