package com.wix.build.sync.cli

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.wix.build.maven._
import com.wix.build.maven.mapper.Mapper

import java.io.InputStream

object RulesMavenThirdPartyDomain {
  private val mapper = Mapper.mapper

  def convertJsonStringToMavenDep(inputStream: InputStream): List[Dependency] = {
    val rulesJvmExternalDeps = mapper.readValue(inputStream, classOf[Array[RulesMavenThirdPartyDependency]])
    rulesJvmExternalDeps.map(toMavenDependency).toList
  }

  private def toMavenDependency(d: RulesMavenThirdPartyDependency) = Dependency(
    coordinates = Coordinates(
      groupId = d.group,
      artifactId = d.artifact,
      version = d.version,
      packaging = d.packaging.map(Packaging).getOrElse(Packaging("jar")),
      classifier = d.classifier
    ),
    scope = MavenScope.Compile,
    isNeverLink = d.neverLink.contains(true),
    isTestOnly = d.testonly.contains(true),
    exclusions = d.exclusions.map(_.map(e => Exclusion(e.group, e.artifact))).getOrElse(Set.empty),
    tags = d.tags.getOrElse(Set.empty),
    aliases = d.aliases.getOrElse(Set.empty),
    flattenTransitiveDeps = d.flattenTransitiveDeps.contains(true)
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  private case class RulesMavenThirdPartyDependency(group: String,
                                                    artifact: String,
                                                    version: String,
                                                    packaging: Option[String],
                                                    classifier: Option[String],
                                                    exclusions: Option[Set[RulesMavenThirdPartyExclusion]],
                                                    @JsonProperty("neverlink")
                                                    neverLink: Option[Boolean],
                                                    testonly: Option[Boolean],
                                                    tags: Option[Set[String]],
                                                    aliases: Option[Set[String]],
                                                    @JsonProperty("flatten_transitive_deps")
                                                    flattenTransitiveDeps: Option[Boolean])

  @JsonIgnoreProperties(ignoreUnknown = true)
  private case class RulesMavenThirdPartyExclusion(group: String, artifact: String)

}
