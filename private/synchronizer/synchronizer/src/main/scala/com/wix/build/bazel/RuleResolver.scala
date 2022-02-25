package com.wix.build.bazel

import com.wix.build.maven._

class RuleResolver(localWorkspaceName: String, thirdPartyDestination: String) {

  def `for`(artifact: Coordinates,
            runtimeDependencies: Set[BazelDep] = Set.empty,
            compileTimeDependencies: Set[BazelDep] = Set.empty,
            transitiveClosureDeps: Set[BazelDep] = Set.empty,
            exclusions: Set[Exclusion] = Set.empty,
            checksum: Option[String] = None,
            srcChecksum: Option[String] = None,
            snapshotSources: Boolean = false,
            neverlink: Boolean = false,
            aliases: Set[String] = Set.empty,
            tags: Set[String] = Set.empty): RuleToPersist =
    artifact.packaging match {
      case Packaging("jar") => RuleToPersist(
        ImportExternalRule.of(artifact,
          aliases,
          tags,
          runtimeDependencies,
          compileTimeDependencies,
          transitiveClosureDeps,
          exclusions,
          checksum,
          srcChecksum,
          snapshotSources,
          neverlink = neverlink),
        ImportExternalRule.ruleLocatorFrom(artifact))
      case Packaging("pom") => RuleToPersist(
        LibraryRule.pomLibraryRule(artifact,
          runtimeDependencies,
          compileTimeDependencies,
          exclusions),
        LibraryRule.packageNameBy(artifact, thirdPartyDestination))
      case _ => throw new RuntimeException(s"no rule defined for ${artifact.serialized}")
    }
}

trait RuleWithDeps {
  val name: String
  val runtimeDeps: Set[String]
  val compileTimeDeps: Set[String]

  def updateDeps(runtimeDeps: Set[String], compileTimeDeps: Set[String]): RuleWithDeps
}

case class RuleToPersist(rule: RuleWithDeps, ruleTargetLocator: String) {
  def withUpdateDeps(runtimeDeps: Set[String], compileTimeDeps: Set[String]): RuleToPersist = {
    copy(rule = rule.updateDeps(runtimeDeps = rule.runtimeDeps ++ runtimeDeps,
      compileTimeDeps = rule.compileTimeDeps ++ compileTimeDeps))
  }
}
