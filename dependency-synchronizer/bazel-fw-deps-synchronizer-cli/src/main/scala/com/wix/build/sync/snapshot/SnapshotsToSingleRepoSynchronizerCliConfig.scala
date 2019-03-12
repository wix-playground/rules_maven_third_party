package com.wix.build.sync.snapshot

case class SnapshotsToSingleRepoSynchronizerCliConfig(mavenRemoteRepositoryURL: List[String],
                                                      targetRepoUrl: String,
                                                      managedDepsRepoUrl: String,
                                                      snapshotToSync: String)

object SnapshotsToSingleRepoSynchronizerCliConfig {
  val BinariesRepoFlag = "binary-repo"
  private val wixRepos = List(
    "http://repo.example.com:80/artifactory/libs-releases",
    "http://repo.example.com:80/artifactory/libs-snapshots")

  val TargetRepoFlag = "target_repo"
  val ManagedDepsRepoFlag = "managed_deps_repo"
  val SnapshotModuleToSyncCoordinatesFlag = "snapshot_modules"

  private val Usage =
    s"""Usage: DependencySynchronizer [--$BinariesRepoFlag remoteRepoUrl] --$TargetRepoFlag targetRepoLocalPath --$ManagedDepsRepoFlag managedDepsRepoLocalPath --$SnapshotModuleToSyncCoordinatesFlag snapshot-module-coordinates --additional-deps dep
      |for example: --target_repo /path/to/target/repo --managed_deps_repo /path/to/managed/deps/repo com.wix.common:wix-framework-leaf:pom:1.0.0-SNAPSHOT --additional-dep com.wix:petri-aspects:1.0.0-SNAPSHOT
    """.stripMargin

  private val parser = new scopt.OptionParser[SnapshotsToSingleRepoSynchronizerCliConfig]("FWDependenciesSynchronizerCli") {
    opt[String](BinariesRepoFlag)
      .withFallback(() => wixRepos.mkString(","))
      .action { case (remoteRepoUrls, config) =>
        config.copy(mavenRemoteRepositoryURL = remoteRepoUrls.split(",").toList) }

    opt[String](TargetRepoFlag)
      .action { case (url, config) =>
        config.copy(targetRepoUrl = url) }

    opt[String](ManagedDepsRepoFlag)
      .action { case (url, config) =>
        config.copy(managedDepsRepoUrl = url) }


    opt[String](SnapshotModuleToSyncCoordinatesFlag)
      .required()
      .action { case (artifact, config) =>
        config.copy(snapshotToSync = artifact) }
  }

  private val Empty = SnapshotsToSingleRepoSynchronizerCliConfig(null, null, null, null)

  def parse(args: Array[String]): SnapshotsToSingleRepoSynchronizerCliConfig = {
    parser.parse(args, Empty).getOrElse(throw new IllegalArgumentException(Usage))
  }
}