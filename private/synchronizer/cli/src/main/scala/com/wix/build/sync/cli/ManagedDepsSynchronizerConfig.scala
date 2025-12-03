package com.wix.build.sync.cli

import com.wix.build.bazel.{DestinationPackage, ImportExternalLoadStatement}
import scopt.OptionParser

object ManagedDepsSynchronizerConfig extends SynchronizerConfigParser

case class ManagedDepsSynchronizerConfig(pathToArtifactsFile: String,
                                         pathToArtifactsOverridesFile: String,
                                         remoteRepositories: List[String],
                                         remoteMavenResolverServerBaseUrl: String,
                                         resolveLocally: Boolean,
                                         localWorkspaceRoot: String,
                                         destination: String,
                                         thirdPartyBzlPath: String,
                                         pollingMaxAttempts: Int,
                                         millisBetweenPollings: Int,
                                         cacheChecksums: Boolean,
                                         importExternalLoadStatement: ImportExternalLoadStatement,
                                         failOnMissingArtifacts: Boolean,
                                         failOnSnapshotVersions: Boolean) {
  val destinationPackage: DestinationPackage = DestinationPackage.resolveFromDestination(destination)
}

abstract class SynchronizerConfigParser {
  val defaultConfiguration = ManagedDepsSynchronizerConfig(
    pathToArtifactsFile = null,
    pathToArtifactsOverridesFile = null,
    remoteRepositories = null,
    remoteMavenResolverServerBaseUrl = null,
    resolveLocally = false,
    localWorkspaceRoot = null,
    destination = "third_party",
    thirdPartyBzlPath = "third_party.bzl",
    pollingMaxAttempts = 200,
    millisBetweenPollings = 3000,
    cacheChecksums = true,
    importExternalLoadStatement = ImportExternalLoadStatement(null, null),
    failOnMissingArtifacts = false,
    failOnSnapshotVersions = false,
  )

  private val parser = new OptionParser[ManagedDepsSynchronizerConfig](toolName) {

    arg[String](name = "<serialized rules_jvm_external artifacts file>")
      .required()
      .text("Path to managed artifacts file with json rep of rules_jvm_external artifacts (one per line)")
      .action { case (path, config) => config.copy(pathToArtifactsFile = path) }

    arg[String](name = "target-repo")
      .required()
      .text("Path to target repository to modify")
      .action { case (path, config) => config.copy(localWorkspaceRoot = path) }

    opt[String](name = "repository-urls")
      .optional()
      .hidden()
      .text("comma delimited list of remote maven repositories (like artifactory)")
      .action {
        case (repos, config) => config.copy(remoteRepositories = repos.split(",").toList)
      }

    opt[String](name = "remote-resolver-url")
      .optional()
      .hidden()
      .action {
        case (remoteResolverUrl, config) => config.copy(remoteMavenResolverServerBaseUrl = remoteResolverUrl)
      }

    opt[Unit](name = "resolve-locally")
      .optional()
      .text("fallback to local resolving (slower, use this if remote resolver server is unresponsive etc.)")
      .action {
        case (_, config) => config.copy(resolveLocally = true)
      }

    opt[Boolean](name = "cache-checksums")
      .optional()
      .text("Use artifact sha calculation caching instead of calculating it after downloading an artifact")
      .action {
        case (cacheChecksums, config) => config.copy(cacheChecksums = cacheChecksums)
      }

    opt[String](name = "destination")
      .required()
      .withFallback(() => "third_party")
      .text("Destination to output sync files. Default is value is third_party, which means outpust will be third_party/ and third_party.bzl")
      .action { case (destination, config) => config.copy(destination = destination) }

    opt[String](name = "third-party-bzl-path")
      .optional()
      .text("Path for the third_party.bzl file")
      .action { case (path, config) => config.copy(thirdPartyBzlPath = path) }

    opt[Int](name = "polling-max-attempts")
      .optional()
      .hidden
      .text("Maximal number of attempts to get the calculation of dependency closure while polling from the remote server")
      .action({ case (num, config) => config.copy(pollingMaxAttempts = num) })

    opt[Int](name = "millis-between-pollings")
      .optional()
      .hidden()
      .text("Time (in milliseconds) to wait between polling requests to the server, when waiting while the dependency closure is calculated")
      .action({ case (millis, config) => config.copy(millisBetweenPollings = millis) })

    opt[String](name = "import-external-macro-name")
      .required()
      .text("Name to be loaded from bzl file to load rule for generated external definitions")
      .action { case (name, config) =>
        config.copy(importExternalLoadStatement = config.importExternalLoadStatement.copy(importExternalMacroName = name))
      }

    opt[String](name = "maven-archive-macro-name")
      .optional()
      .action { case (name, config) =>
        config.copy(importExternalLoadStatement = config.importExternalLoadStatement.copy(mavenArchiveMacroName = name))
      }

    opt[String](name = "import-external-rule-path")
      .required()
      .text("bzl file to load rule from for generated external definitions")
      .action { case (path, config) =>
        config.copy(importExternalLoadStatement = config.importExternalLoadStatement.copy(importExternalRulePath = path))
      }

    opt[Boolean](name = "fail-on-missing-artifacts")
      .optional()
      .text("Fail with an exception, aggregating all missing artifacts, if any")
      .action {
        case (param, config) => config.copy(failOnMissingArtifacts = param)
      }

    opt[Boolean](name = "fail-on-snapshot-versions")
      .optional()
      .text("Fail with an exception, if snapshot versions are detected among managed dependencies")
      .action {
        case (param, config) => config.copy(failOnSnapshotVersions = param)
      }

    help("help")
  }

  def toolName = "bazel run @bazel_tooling//define_maven_deps:managed_deps --" // fixme

  def parse(args: Array[String]): ManagedDepsSynchronizerConfig = {
    parser.parse(args, defaultConfiguration).getOrElse {
      println(parser.usage)
      sys.exit(1)
    }
  }
}

