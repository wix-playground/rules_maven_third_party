package com.wix.build.sync.cli

import com.wix.build.maven._
import com.wix.build.maven.resolver.aether.AetherMavenDependencyResolver
import com.wix.build.sync._
import com.wix.build.sync.core.{DependenciesSynchronizer, ManagedDependenciesSynchronizer}
import org.slf4j.LoggerFactory

import java.io.FileInputStream
import java.nio.file.Paths

object ManagedDepsSynchronizerCli extends SynchronizerCli {
  override def resolver(config: ManagedDepsSynchronizerConfig): MavenDependencyResolver = {
    if (config.resolveLocally)
      new AetherMavenDependencyResolver(config.remoteRepositories)
    else
      throw new IllegalArgumentException(
        "resolveLocally is set to false, but this client does not support dependency " +
          "resolution on a remote server. Use --resolveLocally to use local resolution."
      )
  }
}

abstract class SynchronizerCli {
  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val config = ManagedDepsSynchronizerConfig.parse(args)

    val highLevelDeps = RulesMavenThirdPartyDomain
      .convertJsonStringToMavenDep(new FileInputStream(config.pathToArtifactsFile))

    val artifactsChecksumCache = cache(config)

    // todo: should take single deps parameter
    synchronizer(highLevelDeps, config, artifactsChecksumCache).sync(highLevelDeps)

    artifactsChecksumCache.flush()
  }

  def resolver(config: ManagedDepsSynchronizerConfig): MavenDependencyResolver

  private def synchronizer(managedDeps: List[Dependency],
                           config: ManagedDepsSynchronizerConfig,
                           cache: ArtifactsChecksumCache): DependenciesSynchronizer = {

    val mavenResolver: MavenDependencyResolver = resolver(config)

    val storage = new MavenRepoRemoteStorage(config.remoteRepositories, cache)
    val localPath = Paths.get(config.localWorkspaceRoot)
    log.info("Resolving managed deps")

    new ManagedDependenciesSynchronizer(
      mavenResolver,
      localPath,
      config.destination,
      config.aggregatorPath,
      config.destinationPackage,
      storage,
      managedDeps,
      config.importExternalLoadStatement,
      config.failOnMissingArtifacts,
      config.failOnSnapshotVersions
    )
  }

  private def cache(config: ManagedDepsSynchronizerConfig): ArtifactsChecksumCache = {
    if (config.cacheChecksums) {
      val artifactsChecksumCacheFileAccessor = new ArtifactsChecksumCacheFileAccessor()
      new ArtifactsChecksumFileCache(artifactsChecksumCacheFileAccessor)
    } else {
      new VoidArtifactsShaCache
    }
  }
}

