package com.wix.build.sync

import com.wix.hoopoe.config.ConfigFactory.aConfigFor

object BazelMavenSynchronizerConfig {
  def root: BazelMavenSynchronizerConfig = aConfigFor[BazelMavenSynchronizerConfig]("bazel-deps-synchronizer")
}

case class BazelMavenSynchronizerConfig(
                                         dependencyManagementArtifact: String,
                                         dependencyManagementArtifactBuildTypeId: String,
                                         mavenRemoteRepositoryURL: List[String],
                                         artifactoryUrl: String,
                                         artifactoryToken: String,
                                         git: GitSettings,
                                         branchSuffix: String,
                                       )

case class GitSettings(
                        managedDepsRepoURL: String,
                        managedDepsRepoURLSshFormat: String,
                        username: String,
                        email: String,
                        githubToken: String
                      )