package com.wix.build.sync.cli

import com.wix.build.maven._
import com.wix.build.sync._
import com.wix.build.sync.core.{DependenciesSynchronizer, ManagedDependenciesSynchronizer}
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.io.Source

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

    val serializedArtifacts = getLines(config.pathToArtifactsFile)

    val highLevelDeps = serializedArtifacts.map(RulesJvmExternalDomain.convertJsonStringToMavenDep)

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
      storage,
      managedDeps,
      config.importExternalLoadStatement
    )
  }

  private def cache(config: ManagedDepsSynchronizerConfig): ArtifactsChecksumCache = {
    if (config.artifactShaCache) {
      val artifactsChecksumCacheFileAccessor = new ArtifactsChecksumCacheFileAccessor()
      new ArtifactsChecksumFileCache(artifactsChecksumCacheFileAccessor)
    } else {
      new VoidArtifactsShaCache
    }
  }

  private def getLines(filePath: String): List[String] = {
    val source = Source.fromFile(filePath)
    val lines = source.getLines().toList
    source.close()
    lines
  }
}

