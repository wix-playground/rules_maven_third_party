package com.wix.build.sync.core

import better.files.File
import com.wix.build.bazel.{NeverLinkResolver, NoPersistenceBazelRepository}
import com.wix.build.git.GitAdder
import com.wix.build.maven._
import com.wix.build.sync.ArtifactoryRemoteStorage.decorateNodesWithChecksum
import com.wix.build.sync._
import com.wix.build.sync.WixLoadStatements
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import scala.sys.process.{Process, ProcessLogger}

class LocalDependenciesSynchronizer(mavenDependencyResolver: MavenDependencyResolver,
                                    dependenciesRemoteStorage: DependenciesRemoteStorage,
                                    localRepoPath: File,
                                    destination: String,
                                    managedDependencies: List[Dependency]) extends DependenciesSynchronizer {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def calcManagedNodesToSync(dependencies: List[Dependency]): Set[BazelDependencyNode] = {
    val naiveDependencyGraph = mavenDependencyResolver.dependencyClosureOf(
      dependencies,
      managedDependencies
    )

    // TODO: fix this message
    logger.info(s"syncing ${naiveDependencyGraph.size} dependencies which are the closure of the ${dependencies.size} dependencies")

    if (naiveDependencyGraph.isEmpty)
      Set[BazelDependencyNode]()
    else {
      logger.info("started fetching sha256 checksums for 3rd party dependencies from artifactory...")
      val nodes = decorateNodesWithChecksum(naiveDependencyGraph)(dependenciesRemoteStorage)
      logger.info("completed fetching sha256 checksums.")

      DependencyConfigAugmenter.augment(nodes, managedDependencies)
    }
  }

  override def sync(dependencies: List[Dependency]): Unit = {
    // todo: add support for artifact configs

    val neverLinkResolver = NeverLinkResolver()

    val diffCalculator = new UserAddedDepsDiffCalculator(
      bazelRepo = new NoPersistenceBazelRepository(localRepoPath, destination),
      bazelRepoWithManagedDependencies = cloneMangedDepsRepo(),
      aetherResolver = mavenDependencyResolver,
      ignoreMissingDependenciesFlag = false,
      dependenciesRemoteStorage,
      mavenModulesToTreatAsSourceDeps = Set.empty,
      neverLinkResolver = neverLinkResolver
    )

    new UserAddedDepsDiffSynchronizer(
      diffCalculator,
      DefaultDiffWriter(
        targetRepository = new NoPersistenceBazelRepository(localRepoPath, destination),
        maybeManagedDepsRepoPath = Some(localRepoPath.toString()),
        neverLinkResolver = neverLinkResolver,
        importExternalLoadStatement = WixLoadStatements.importExternalLoadStatement,
        maybeGitAdder = Some(new GitAdder(localRepoPath.path)))).syncThirdParties(dependencies.toSet)

  }

  private def cloneMangedDepsRepo() = {
    val checkoutDir = Files.createTempDirectory("cli-test")
    val repoName = "core-server-build-tools"
    val gitUrl = s"git@github.com:wix-private/$repoName.git"
    logger.info(s"Cloning $gitUrl into $checkoutDir ...")

    Process(s"git clone $gitUrl --shallow-since=${toShallowSince(System.currentTimeMillis())}", checkoutDir.toFile).!!(ProcessLogger(output => logger.info(output)))

    logger.info(s"Clone completed.")

    new NoPersistenceBazelRepository(File(checkoutDir) / repoName, destination)
  }

  private def toShallowSince(systemTime: Long): String = {
    val aDayBefore = systemTime - 3600 * 24
    val date = new Date(aDayBefore * 1000)
    val dateFormat = new SimpleDateFormat("YYYY-MM-dd")
    dateFormat.format(date)
  }
}
