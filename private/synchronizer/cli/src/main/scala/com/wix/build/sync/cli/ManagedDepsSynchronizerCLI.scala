package com.wix.build.sync.cli

import com.wix.build.maven._
import com.wix.build.sync._
import com.wix.build.sync.core.{DependenciesSynchronizer, LocalDependenciesSynchronizer, ManagedDependenciesSynchronizer}
import com.wix.build.maven.MavenDependencyResolverRestClient
import com.wix.build.sync._
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.io.Source

object ManagedDepsSynchronizerCLI {
  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val config = ManagedDepsSynchronizerConfig.parse(args)

    val serializedArtifacts = getLines(config.pathToArtifactsFile)
    val serializedArtifactsOverrides = getLines(config.pathToArtifactsOverridesFile)

    val mavenDeps = serializedArtifacts.map(RulesJvmExternalDomain.convertJsonStringToMavenDep)
    val mavenDepsOverrides = serializedArtifactsOverrides.map(RulesJvmExternalDomain.convertJsonStringToMavenDep)

    val isLocal = serializedArtifacts != serializedArtifactsOverrides
    val artifactsChecksumCache = cache(config)

    synchronizer(mavenDeps, config, isLocal, artifactsChecksumCache).sync(mavenDepsOverrides)

    artifactsChecksumCache.flush()
  }

  private def synchronizer(managedDeps: List[Dependency],
                           config: ManagedDepsSynchronizerConfig,
                           isLocal: Boolean,
                           cache: ArtifactsChecksumCache): DependenciesSynchronizer = {

    val mavenResolver = if (!config.resolveLocally) {
      MavenDependencyResolverRestClient(
        config.remoteMavenResolverServerBaseUrl,
        pollingMaxAttempts = config.pollingMaxAttempts,
        millisToWaitBetweenPollings = config.millisBetweenPollings
      )
    } else {
      new AetherMavenDependencyResolver(config.remoteRepositories)
    }

    val storage = new MavenRepoRemoteStorage(config.remoteRepositories, cache)
    val localPath = Paths.get(config.localWorkspaceRoot)

    if (isLocal) {
      log.info("Resolving local repo overrides")
      new LocalDependenciesSynchronizer(
        mavenResolver,
        storage,
        localPath,
        config.destination,
        managedDeps
      )
    } else {
      log.info("Resolving managed deps")

      new ManagedDependenciesSynchronizer(
        mavenResolver,
        localPath,
        config.destination,
        storage,
        managedDeps,
        WixLoadStatements.importExternalLoadStatement
      )
    }
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


