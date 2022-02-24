package com.wix.build.sync.cli

import com.wix.build.bazel.ImportExternalLoadStatement
import scopt.OptionParser

object ManagedDepsSynchronizerConfig extends SynchronizerConfigParser {

}

case class ManagedDepsSynchronizerConfig(pathToArtifactsFile: String,
                                         pathToArtifactsOverridesFile: String,
                                         remoteRepositories: List[String],
                                         remoteMavenResolverServerBaseUrl: String,
                                         resolveLocally: Boolean,
                                         localWorkspaceRoot: String,
                                         destination: String,
                                         pollingMaxAttempts: Int,
                                         millisBetweenPollings: Int,
                                         artifactShaCache: Boolean,
                                         importExternalLoadStatement: ImportExternalLoadStatement)

abstract class SynchronizerConfigParser {
  val TargetRepoFlag = "targetRepo"
  val ReposFlag = "remoteMavenRepos"

  val ResolveLocallyFlag = "resolveLocally"
  val ArtifactShaCacheFlag = "artifactShaCache"

  val DestinationFlag = "destination"

  val ArtifactIdToDebug = "artifactToDebug"
  val IgnoreMissingDependenciesFlag = "ignoreMissingDependencies"

  private val PollingMaxAttempts = "pollingMaxAttempts"
  private val MillisBetweenPollings = "millisBetweenPollings"

  private val wixRepos = List(
    "https://repo.example.com/artifactory/libs-snapshots",
    "https://repo.example.com/artifactory/libs-releases"
  )

  private val mavenResolverServer = "https://server.example.com"

  val defaultConfiguration = ManagedDepsSynchronizerConfig(
    pathToArtifactsFile = null,
    pathToArtifactsOverridesFile = null,
    remoteRepositories = wixRepos,
    remoteMavenResolverServerBaseUrl = mavenResolverServer,
    resolveLocally = false,
    localWorkspaceRoot = null,
    destination = "third_party",
    pollingMaxAttempts = 200,
    millisBetweenPollings = 3000,
    artifactShaCache = true,
    importExternalLoadStatement = ImportExternalLoadStatement(null, null),
  )

  private val parser = new OptionParser[ManagedDepsSynchronizerConfig](toolName) {

    arg[String](name = "<serialized rules_jvm_external artifacts file>")
      .required()
      .text("Path to managed artifacts file with json rep of rules_jvm_external artifacts (one per line)")
      .action { case (path, config) => config.copy(pathToArtifactsFile = path) }

    arg[String](name = TargetRepoFlag)
      .required()
      .text("Path to target repository to modify")
      .action { case (path, config) => config.copy(localWorkspaceRoot = path) }

    opt[String](name = ReposFlag)
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

    opt[Unit](name = ResolveLocallyFlag)
      .optional()
      .text("fallback to local resolving (slower, use this if remote resolver server is unresponsive etc.)")
      .action {
        case (_, config) => config.copy(resolveLocally = true)
      }

    opt[Unit](name = ArtifactShaCacheFlag)
      .optional()
      .text("Use artifact sha calculation caching instead of calculating it after downloading an artifact")
      .action {
        case (_, config) => config.copy(artifactShaCache = true)
      }

    opt[String](name = DestinationFlag)
      .required()
      .withFallback(() => "third_party")
      .text("Destination to output sync files. Default is value is third_party, which means outpust will be third_party/ and third_party.bzl")
      .action { case (destination, config) => config.copy(destination = destination) }

    opt[Int](name = PollingMaxAttempts)
      .optional()
      .hidden
      .text("Maximal number of attempts to get the calculation of dependency closure while polling from the remote server")
      .action({ case (num, config) => config.copy(pollingMaxAttempts = num) })

    opt[Int](name = MillisBetweenPollings)
      .optional()
      .hidden()
      .text("Time (in milliseconds) to wait between polling requests to the server, when waiting while the dependency closure is calculated")
      .action({ case (millis, config) => config.copy(millisBetweenPollings = millis) })

    opt[String](name = "import_external_macro_name")
      .required()
      .text("Name to be loaded from bzl file to load rule for generated external definitions")
      .action { case (name, config) =>
        config.copy(importExternalLoadStatement = config.importExternalLoadStatement.copy(importExternalMacroName = name))
      }

    opt[String](name = "import_external_rule_path")
      .required()
      .text("bzl file to load rule for generated external definitions")
      .action { case (path, config) =>
        config.copy(importExternalLoadStatement = config.importExternalLoadStatement.copy(importExternalRulePath = path))
      }


    help("help")
  }

  def toolName = "bazel run @bazel_tooling//define_maven_deps:managed_deps --"

  def parse(args: Array[String]): ManagedDepsSynchronizerConfig = {
    parser.parse(args, defaultConfiguration).getOrElse {
      println(parser.usage)
      sys.exit(1)
    }
  }
}

