package com.wix.build.sync.fw

case class FWDependenciesSynchronizerConfig(mavenRemoteRepositoryURL: List[String],
                  managedDepsRepoUrl: String,
                  fwArtifact: String,
                  additionalDeps: List[String])

object FWDependenciesSynchronizerConfig {
  private val RepoFlag = "binary-repo"
  //TODO: move to wix-maven-resolver module
  private val wixRepos = List(
    "http://repo.example.com:80/artifactory/libs-releases",
    "http://repo.example.com:80/artifactory/libs-snapshots")

  private val managedDepsRepoFlag = "managed_deps_repo"
  private val fwLeafArtifactFlag = "fw-leaf-artifact"
  private val additionalDepsFlag = "additional-deps"

  private val Usage =
    s"""Usage: DependencySynchronizer [--$RepoFlag remoteRepoUrl] --$managedDepsRepoFlag managedDepsRepoLocalPath --$fwLeafArtifactFlag fw-leaf-coordinates --additional-deps dep
      |for example: --managed_deps_repo /path/to/managed/deps/repo com.wix.common:wix-framework-leaf:pom:1.0.0-SNAPSHOT --additional-dep com.wix:petri-aspects:1.0.0-SNAPSHOT
    """.stripMargin

  private val parser = new scopt.OptionParser[FWDependenciesSynchronizerConfig]("FWDependenciesSynchronizerCli") {
    opt[String](RepoFlag)
      .withFallback(() => wixRepos.mkString(","))
      .action { case (remoteRepoUrls, config) =>
        config.copy(mavenRemoteRepositoryURL = remoteRepoUrls.split(",").toList) }

    opt[String](managedDepsRepoFlag)
      .action { case (url, config) =>
        config.copy(managedDepsRepoUrl = url) }

    opt[String](fwLeafArtifactFlag)
      .required()
      .action { case (artifact, config) =>
        config.copy(fwArtifact = artifact) }

    opt[String](additionalDepsFlag)
      .action { case (urls, config) =>
        config.copy(additionalDeps = urls.split(",").toList) }
  }

  private val Empty = FWDependenciesSynchronizerConfig(null, null, null, List())

  def parse(args: Array[String]): FWDependenciesSynchronizerConfig = {
    parser.parse(args, Empty).getOrElse(throw new IllegalArgumentException(Usage))
  }
}