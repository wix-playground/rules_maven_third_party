package com.wix.build.sync.snapshot

case class FWDependenciesToSingleRepoSynchronizerConfig(mavenRemoteRepositoryURL: List[String],
                                                        targetRepoUrl: String,
                                                        managedDepsRepoUrl: String,
                                                        fwArtifact: String)

object FWDependenciesToSingleRepoSynchronizerConfig {
  private val RepoFlag = "binary-repo"
  private val wixRepos = List(
    "http://repo.example.com:80/artifactory/libs-releases",
    "http://repo.example.com:80/artifactory/libs-snapshots")

  private val targetRepoFlag = "target_repo"
  private val managedDepsRepoFlag = "managed_deps_repo"
  private val snapshotModuleCoordinatesFlag = "snapshot_modules"

  private val Usage =
    s"""Usage: DependencySynchronizer [--$RepoFlag remoteRepoUrl] --$targetRepoFlag targetRepoLocalPath --$managedDepsRepoFlag managedDepsRepoLocalPath --$snapshotModuleCoordinatesFlag snapshot-module-coordinates --additional-deps dep
      |for example: --target_repo /path/to/target/repo --managed_deps_repo /path/to/managed/deps/repo com.wix.common:wix-framework-leaf:pom:1.0.0-SNAPSHOT --additional-dep com.wix:petri-aspects:1.0.0-SNAPSHOT
    """.stripMargin

  private val parser = new scopt.OptionParser[FWDependenciesToSingleRepoSynchronizerConfig]("FWDependenciesSynchronizerCli") {
    opt[String](RepoFlag)
      .withFallback(() => wixRepos.mkString(","))
      .action { case (remoteRepoUrls, config) =>
        config.copy(mavenRemoteRepositoryURL = remoteRepoUrls.split(",").toList) }

    opt[String](targetRepoFlag)
      .action { case (url, config) =>
        config.copy(targetRepoUrl = url) }

    opt[String](managedDepsRepoFlag)
      .action { case (url, config) =>
        config.copy(managedDepsRepoUrl = url) }


    opt[String](snapshotModuleCoordinatesFlag)
      .required()
      .action { case (artifact, config) =>
        config.copy(fwArtifact = artifact) }
  }

  private val Empty = FWDependenciesToSingleRepoSynchronizerConfig(null, null, null, null)

  def parse(args: Array[String]): FWDependenciesToSingleRepoSynchronizerConfig = {
    parser.parse(args, Empty).getOrElse(throw new IllegalArgumentException(Usage))
  }
}