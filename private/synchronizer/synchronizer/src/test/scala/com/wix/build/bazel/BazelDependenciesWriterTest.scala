package com.wix.build.bazel

import com.wix.build.bazel.FakeLocalBazelWorkspace.thirdPartyReposFilePath
import com.wix.build.bazel.LibraryRule.packageNameBy
import com.wix.build.bazel.ThirdPartyOverridesMakers.{compileTimeOverrides, overrideCoordinatesFrom, runtimeOverrides}
import com.wix.build.maven.DefaultChecksumValues._
import com.wix.build.maven.MavenMakers._
import com.wix.build.maven._
import com.wix.build.translation.MavenToBazelTranslations._
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import scala.util.matching.Regex

//noinspection TypeAnnotation
class BazelDependenciesWriterTest extends SpecificationWithJUnit {

  "BazelDependenciesWriter " >> {

    trait emptyThirdPartyReposCtx extends Scope {
      val localWorkspaceName = "some_workspace_name"
      val thirdPartyPath = "third_party"
      val localWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = localWorkspaceName, thirdPartyPaths = new ThirdPartyPaths(thirdPartyPath))

      def writer = writerFor(localWorkspace)

      def labelOfPomArtifact(dependency: Dependency) = {
        val coordinates = dependency.coordinates
        s"@//${packageNameBy(coordinates, thirdPartyPath)}:${coordinates.libraryRuleName}"
      }

      def labelOfJarArtifact(dependency: Dependency) = {
        val coordinates = dependency.coordinates
        s"@${coordinates.workspaceRuleName}"
      }

      localWorkspace.overwriteThirdPartyReposFile("")
      localWorkspace.overwriteLocalArtifactOverridesFile("LOCAL_OVERRIDE_DEPS = []")
    }

    "given no dependencies" should {

      "write 'pass' to third party repos file" in new emptyThirdPartyReposCtx {
        writer.writeDependencies()

        localWorkspace.thirdPartyReposFileContent() must contain("pass")
      }
    }

    "given one new root dependency" should {
      trait newRootDependencyNodeCtx extends emptyThirdPartyReposCtx {
        val baseDependency = aDependency("some-dep")
        val baseSnapshotDependency = aDependency("some-dep-SNAPSHOT")
        val providedDependency = aDependency(artifactId = "some-dep", scope = MavenScope.Provided)
        val matchingGroupId = baseDependency.coordinates.groupIdForBazel
      }

      "write import_external rule with checksum to third party repos file " in new newRootDependencyNodeCtx {
        writer.writeDependencies(aRootBazelDependencyNode(baseDependency, checksum = Some("checksum"), srcChecksum = Some("srcChecksum")))

        localWorkspace.thirdPartyImportTargetsFileContent(matchingGroupId) must
          containRootScalaImportExternalRuleFor(baseDependency.coordinates, "checksum", "srcChecksum")
      }

      "write maven artifact coordinates to local artifact overrides file" in new newRootDependencyNodeCtx {
        writer.writeDependencies(
          Set(baseDependency),
          Set(aRootBazelDependencyNode(baseDependency)),
          Set.empty[BazelDependencyNode],
          Set.empty[Coordinates]
        )

        localWorkspace.localArtifactOverridesFileContent() must containMavenArtifact(baseDependency.coordinates)
      }

      "write import_external rule with snapshotSources=1 to third party repos file " in new newRootDependencyNodeCtx {
        writer.writeDependencies(aRootBazelSnapshotDependencyNode(baseSnapshotDependency))

        localWorkspace.thirdPartyImportTargetsFileContent(matchingGroupId) must
          containRootSnapshotScalaImportExternalRuleFor(baseSnapshotDependency.coordinates, missingJar = true)
      }

      "write import_external rule with neverlink and linkable rule name to third party repos file " in new newRootDependencyNodeCtx {
        writer.writeDependencies(aRootBazelDependencyNode(providedDependency))

        localWorkspace.thirdPartyImportTargetsFileContent(matchingGroupId) must
          containScalaImportExternalRuleFor(providedDependency.coordinates,
            s"""|neverlink = 1,
                |generated_linkable_rule_name = "linkable",""".stripMargin)
      }
    }

    "given one new proto dependency" should {
      trait protoDependencyNodeCtx extends emptyThirdPartyReposCtx {
        val protoCoordinates = Coordinates("some.group", "some-artifact", "version", Packaging("zip"), Some("proto"))
        val protoDependency = Dependency(protoCoordinates, MavenScope.Compile)
      }

      "write maven_proto rule to third party repos file" in new protoDependencyNodeCtx {
        writer.writeDependencies(aRootBazelDependencyNode(protoDependency))

        localWorkspace.thirdPartyReposFileContent() must containMavenProtoRuleFor(protoCoordinates)
      }

      "change only third party repos file" in new protoDependencyNodeCtx {
        val node = aRootBazelDependencyNode(protoDependency)
        val changedFiles = writer.writeDependencies(node)

        changedFiles must contain(exactly(thirdPartyReposFilePath))
      }

    }

    "given one new dependency with transitive dependencies" should {
      abstract class dependencyWithTransitiveDependencyofScope(scope: MavenScope) extends emptyThirdPartyReposCtx {
        val baseDependency = aDependency("base")
        val transitiveDependency = aDependency("transitive", scope)
        val dependencyNode = BazelDependencyNode(baseDependency, Set(transitiveDependency), Some(defaultChecksum), Some(defaultSrcChecksum))

        val dependencyGroupId = baseDependency.coordinates.groupIdForBazel
      }
      "write target with runtime dependency" in new dependencyWithTransitiveDependencyofScope(MavenScope.Runtime) {
        writer.writeDependencies(dependencyNode)

        localWorkspace.thirdPartyReposFileContent() must containLoadStatementForGroupOf(baseDependency.coordinates)

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must containScalaImportExternalRuleFor(baseDependency.coordinates,
          s"""|runtime_deps = [
              |    "${labelOfJarArtifact(transitiveDependency)}",
              |],""".stripMargin)
      }

      "write target with compile time dependency" in new dependencyWithTransitiveDependencyofScope(MavenScope.Compile) {
        writer.writeDependencies(dependencyNode)

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must containScalaImportExternalRuleFor(baseDependency.coordinates,
          s"""|    deps = [
              |     "${labelOfJarArtifact(transitiveDependency)}",
              |    ],""".stripMargin)
      }

      "write target with compile time pom artifact dependency" in new emptyThirdPartyReposCtx {
        val baseDependency = aDependency("base")
        val transitiveDependency = aPomArtifactDependency("transitive", MavenScope.Compile)
        val dependencyNode = aBazelDependencyNode(baseDependency, Set(transitiveDependency))
        val dependencyGroupId = baseDependency.coordinates.groupIdForBazel

        writer.writeDependencies(dependencyNode)

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must containScalaImportExternalRuleFor(baseDependency.coordinates,
          s"""|    deps = [
              |     "${labelOfPomArtifact(transitiveDependency)}",
              |    ],""".stripMargin)
      }

      "write a target that is originated from pom artifact and has transitive jar artifact" in new emptyThirdPartyReposCtx {
        val baseCoordinates = Coordinates("some.group", "some-artifact", "some-version", Packaging("pom"))
        val baseDependency = Dependency(baseCoordinates, MavenScope.Compile)
        val transitiveJarArtifactDependency = aDependency("transitive")
        val dependencyNode = BazelDependencyNode(baseDependency, Set(transitiveJarArtifactDependency))

        writer.writeDependencies(dependencyNode)

        val maybeBuildFile: Option[String] = localWorkspace.buildFileContent(packageNameBy(baseCoordinates, thirdPartyPath))
        maybeBuildFile must beSome(
          containsIgnoringSpaces(
            s"""scala_import(
               |    name = "${baseDependency.coordinates.libraryRuleName}",
               |    exports = [
               |       "${labelOfJarArtifact(transitiveJarArtifactDependency)}",
               |    ],
               |)""".stripMargin
          ))
      }

      "write a target that is originated from pom artifact and has transitive pom artifact" in new emptyThirdPartyReposCtx {
        val baseCoordinates = Coordinates("some.group", "some-artifact", "some-version", Packaging("pom"))
        val baseDependency = Dependency(baseCoordinates, MavenScope.Compile)
        val transitivePomArtifactDependency = aPomArtifactDependency("transitive")
        val dependencyNode = BazelDependencyNode(baseDependency, Set(transitivePomArtifactDependency))

        writer.writeDependencies(dependencyNode)

        val maybeBuildFile: Option[String] = localWorkspace.buildFileContent(packageNameBy(baseCoordinates, thirdPartyPath))
        maybeBuildFile must beSome(
          containsIgnoringSpaces(
            s"""scala_import(
               |    name = "${baseDependency.coordinates.libraryRuleName}",
               |    exports = [
               |       "${labelOfPomArtifact(transitivePomArtifactDependency)}",
               |    ],
               |)""".stripMargin
          ))
      }

      "write target with multiple dependencies" in new emptyThirdPartyReposCtx {
        val baseDependency = aDependency("base")
        val transitiveDependencies = {
          1 to 5
        }.map(index => aDependency(s"transitive$index")).reverse
        val dependencyNode = aBazelDependencyNode(baseDependency, transitiveDependencies.toSet)
        val serializedLabelsOfTransitiveDependencies = transitiveDependencies
          .map(labelOfJarArtifact)
          .sorted
          .map(label => s""""$label"""")
          .mkString(",\n")

        writer.writeDependencies(dependencyNode)

        localWorkspace.thirdPartyImportTargetsFileContent(baseDependency.coordinates.groupIdForBazel) must
          containScalaImportExternalRuleFor(baseDependency.coordinates,
            s"""|    deps = [
                |      $serializedLabelsOfTransitiveDependencies,
                |    ],""".stripMargin
          )

      }

      "write target with exclusion" in new emptyThirdPartyReposCtx {
        val exclusion = Exclusion("some.excluded.group", "some-excluded-artifact")
        val baseDependency = aDependency("base").copy(exclusions = Set(exclusion))
        val dependencyNode = aRootBazelDependencyNode(baseDependency)

        writer.writeDependencies(dependencyNode)

        localWorkspace.thirdPartyImportTargetsFileContent(baseDependency.coordinates.groupIdForBazel) must containScalaImportExternalRuleFor(
          baseDependency.coordinates,
          s"""|    excludes = [
              |      "${exclusion.serialized}",
              |    ],""".stripMargin

        )
      }

      "write target with runtime dependencies from overrides" in new dependencyWithTransitiveDependencyofScope(MavenScope.Runtime) {
        def baseDependencyCoordinates = baseDependency.coordinates

        def customRuntimeDependency = "some_runtime_dep"

        override def writer: BazelDependenciesWriter = writerFor(localWorkspace)

        localWorkspace.setThirdPartyOverrides(
          runtimeOverrides(overrideCoordinatesFrom(baseDependencyCoordinates), customRuntimeDependency)
        )

        writer.writeDependencies(dependencyNode)

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must containScalaImportExternalRuleFor(
          baseDependency.coordinates,
          s"""runtime_deps = [
             |     "${labelOfJarArtifact(transitiveDependency)}",
             |     "$customRuntimeDependency",
             |    ],""".stripMargin
        )
      }

      "write target with compile time dependencies from overrides" in new dependencyWithTransitiveDependencyofScope(MavenScope.Compile) {
        def baseDependencyCoordinates = baseDependency.coordinates

        def customCompileTimeDependency = "some_compile_dep"

        override def writer: BazelDependenciesWriter = writerFor(localWorkspace)

        localWorkspace.setThirdPartyOverrides(compileTimeOverrides(overrideCoordinatesFrom(baseDependencyCoordinates), customCompileTimeDependency))

        writer.writeDependencies(dependencyNode)

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must containScalaImportExternalRuleFor(
          baseDependency.coordinates,
          s"""deps = [
             |     "${labelOfJarArtifact(transitiveDependency)}",
             |     "$customCompileTimeDependency",
             |    ],""".stripMargin
        )
      }
    }

    "given dependency with transitive closure deps specified" should {
      trait transitiveClosureDependencyNodeCtx extends emptyThirdPartyReposCtx {
        val baseDependency = aDependency("some-dep")
        val transitiveDep: Dependency = aDependency("transitive-dep")
        val dependencyNode = aRootBazelDependencyNode(baseDependency)
          .copy(transitiveClosureDeps = Set(transitiveDep))

        writer.writeDependencies(dependencyNode)
        val dependencyGroupId = baseDependency.coordinates.groupIdForBazel
      }
      "write transitive_closure_deps attribute" in new transitiveClosureDependencyNodeCtx {

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must containScalaImportExternalRuleFor(
          baseDependency.coordinates, withExtraParams =
            """transitive_closure_deps = [
              |"@some_group_transitive_dep",
              |],""".stripMargin
        )
      }
    }

    "given one dependency that already exists in the workspace " should {
      trait updateDependencyNodeCtx extends emptyThirdPartyReposCtx {
        val originalBaseDependency = aDependency("some-dep")
        val originalDependencyNode = aRootBazelDependencyNode(originalBaseDependency)
        writer.writeDependencies(originalDependencyNode)
        val dependencyGroupId = originalBaseDependency.coordinates.groupIdForBazel
      }

      "update version of import_external rule" in new updateDependencyNodeCtx {
        val newDependency = originalBaseDependency.withVersion("other-version")

        writer.writeDependencies(aRootBazelDependencyNode(newDependency))

        val workspaceContent = localWorkspace.thirdPartyReposFileContent()

        workspaceContent must containLoadStatementForGroupOf(newDependency.coordinates)

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must containRootScalaImportExternalRuleFor(
          newDependency.coordinates)

        localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId) must beSome(containsExactlyOneRuleOfName(originalBaseDependency.coordinates.workspaceRuleName))
      }

      "update dependencies of import external rule" in new updateDependencyNodeCtx {
        val newTransitiveDependency = aDependency("transitive")
        val newDependencyNode = aBazelDependencyNode(originalBaseDependency, Set(newTransitiveDependency))

        writer.writeDependencies(newDependencyNode)

        val importExternalFileContent = localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId)

        importExternalFileContent must containScalaImportExternalRuleFor(originalBaseDependency.coordinates,
          s"""|    deps = [
              |      "${labelOfJarArtifact(newTransitiveDependency)}",
              |    ],""".stripMargin
        )
        importExternalFileContent must beSome(containsExactlyOneRuleOfName(originalBaseDependency.coordinates.workspaceRuleName))
      }

      "update exclusions in library rule" in new updateDependencyNodeCtx {
        val someExclusion = Exclusion("some.excluded.group", "some-excluded-artifact")
        val newBaseDependency = originalBaseDependency.copy(exclusions = Set(someExclusion))
        val newDependencyNode = originalDependencyNode.copy(baseDependency = newBaseDependency)

        writer.writeDependencies(newDependencyNode)

        val importExternalFileContent = localWorkspace.thirdPartyImportTargetsFileContent(dependencyGroupId)

        importExternalFileContent must containScalaImportExternalRuleFor(originalBaseDependency.coordinates,
          s"""|    excludes = [
              |      "${someExclusion.serialized}",
              |    ],""".stripMargin
        )
        importExternalFileContent must beSome(containsExactlyOneRuleOfName(originalBaseDependency.coordinates.workspaceRuleName))
      }

    }

    "given multiple dependencies" should {
      trait multipleDependenciesCtx extends emptyThirdPartyReposCtx {
        val someArtifact = Coordinates("some.group", "artifact-one", "some-version")
        val otherArtifact = Coordinates("other.group", "artifact-two", "some-version")

        def writeArtifactsAsRootDependencies(artifacts: Coordinates*) = {
          val dependencyNodes = artifacts.map(a => aRootBazelDependencyNode(Dependency(a, MavenScope.Compile)))
          writer.writeDependencies(dependencyNodes: _*)
        }
      }

      "write multiple targets to the same bzl file, in case same groupId" in new multipleDependenciesCtx {
        val otherArtifactWithSameGroupId = someArtifact.copy(artifactId = "other-artifact")

        writeArtifactsAsRootDependencies(someArtifact, otherArtifactWithSameGroupId)

        val importExternalFile = localWorkspace.thirdPartyImportTargetsFileContent(someArtifact.groupIdForBazel)
        importExternalFile must containRootScalaImportExternalRuleFor(someArtifact)
        importExternalFile must containRootScalaImportExternalRuleFor(otherArtifactWithSameGroupId)
      }

      "write multiple targets, sorted according to target name, to the same bzl file, in case same groupId" in new multipleDependenciesCtx {
        val artifactA = Coordinates("some.group", "artifact-a", "some-version")
        val artifactB = artifactA.copy(artifactId = "artifact-b")
        val artifactC = artifactA.copy(artifactId = "artifact-c")
        val artifactD = artifactA.copy(artifactId = "artifact-d")

        writeArtifactsAsRootDependencies(artifactB, artifactA, artifactD, artifactC)

        val importExternalFile = localWorkspace.thirdPartyImportTargetsFileContent(artifactA.groupIdForBazel)
        importExternalFile must containSortedTargets
      }

      "write multiple load statements to third party repos file" in new multipleDependenciesCtx {
        writeArtifactsAsRootDependencies(someArtifact, otherArtifact)

        val workspace = localWorkspace.thirdPartyReposFileContent()
        workspace must containLoadStatementForGroupOf(someArtifact)
        workspace must containLoadStatementForGroupOf(otherArtifact)
      }

      "return list of all files that were written" in new multipleDependenciesCtx {
        val writtenFiles = writeArtifactsAsRootDependencies(someArtifact, otherArtifact.copy(packaging = Packaging("pom")))

        writtenFiles must containTheSameElementsAs(Seq(
          thirdPartyReposFilePath,
          ImportExternalRule.importExternalFilePathBy(someArtifact, thirdPartyPath).get,
          LibraryRule.buildFilePathBy(otherArtifact, thirdPartyPath).get)
        )
      }
    }

    "given an overrided set of neverlink coordinates" should {
      "write target with 'neverlink = 1 ' if came from overrided list" in {
        val localWorkspaceName = "some_workspace_name"
        val localWorkspace = new FakeLocalBazelWorkspace(localWorkspaceName = localWorkspaceName, thirdPartyPaths = new ThirdPartyPaths("third_party"))

        val artifact = someCoordinates("some-artifact")

        def writer = writerFor(
          localWorkspace,
          NeverLinkResolver(overrideGlobalNeverLinkDependencies = Set(artifact))
        )

        writer.writeDependencies(aRootBazelDependencyNode(asCompileDependency(artifact)))

        val importExternalFileContent = localWorkspace.thirdPartyImportTargetsFileContent(artifact.groupIdForBazel)

        importExternalFileContent must containScalaImportExternalRuleFor(artifact,
          s"""|    neverlink = 1,
              |    generated_linkable_rule_name = "linkable",""".stripMargin
        )
      }

      "write target with 'linkable' transitive dep if came from global overrided list and is missing from local neverlink " in {
        val localWorkspace = new FakeLocalBazelWorkspace()
        val artifact = someCoordinates("some-artifact")
        val transitiveDep = someCoordinates("some-transitiveDep")

        val newTransitiveDependency = asCompileDependency(transitiveDep)
        val newDependencyNode = aBazelDependencyNode(asCompileDependency(artifact), Set(newTransitiveDependency))

        def writer = writerFor(
          localWorkspace,
          NeverLinkResolver(overrideGlobalNeverLinkDependencies = Set(transitiveDep))
        )

        writer.writeDependencies(newDependencyNode)

        val importExternalFileContent = localWorkspace.thirdPartyImportTargetsFileContent(artifact.groupIdForBazel)

        importExternalFileContent must containScalaImportExternalRuleFor(artifact,
          s"""|    deps = [
              |     "@${newTransitiveDependency.coordinates.workspaceRuleName}//:linkable",
              |    ],""".stripMargin)

      }
    }

    "given a localDepToDelete" should {
      trait depsToDeleteCtx extends emptyThirdPartyReposCtx {
        val group1 = "some.group1"
        val group2 = "some.group2"
        val someArtifact = "artifact-one"
        val someArtifactGroup1 = Coordinates(group1, someArtifact, "some-version")
        val otherArtifactGroup1 = Coordinates(group1, "artifact-two", "some-version")
        val otherArtifactGroup2 = Coordinates(group2, "artifact-three", "some-version")

        def writeArtifactsAsRootDependencies(artifacts: Coordinates*) = {
          val dependencyNodes = artifacts.map(a => aRootBazelDependencyNode(Dependency(a, MavenScope.Compile)))
          writer.writeDependencies(dependencyNodes: _*)
        }
      }

      "delete it's target in bzl file but keep it's repo rule if group still in use" in new depsToDeleteCtx {
        writeArtifactsAsRootDependencies(someArtifactGroup1, otherArtifactGroup1)

        writer.writeDependencies(Set(), Set(), Set(), localDepsToDelete = Set(someArtifactGroup1))

        val workspace = localWorkspace.thirdPartyReposFileContent()
        workspace must containLoadStatementForGroupOf(otherArtifactGroup1)

        val importExternalFileContent = localWorkspace.thirdPartyImportTargetsFileContent(otherArtifactGroup1.groupIdForBazel)
        importExternalFileContent must containRootScalaImportExternalRuleFor(otherArtifactGroup1)
        importExternalFileContent must not(containRootScalaImportExternalRuleFor(someArtifactGroup1))
      }

      "delete it's bzl file if no targets left and also delete it's load statement" in new depsToDeleteCtx {
        writeArtifactsAsRootDependencies(someArtifactGroup1, otherArtifactGroup2)

        writer.writeDependencies(Set(), Set(), Set(), localDepsToDelete = Set(someArtifactGroup1))

        val workspace = localWorkspace.thirdPartyReposFileContent()
        workspace must not(containLoadStatementForGroupOf(someArtifactGroup1))
        workspace must containLoadStatementForGroupOf(otherArtifactGroup2)

        val importExternalFileContent = localWorkspace.thirdPartyImportTargetsFileContent(someArtifactGroup1.groupIdForBazel)
        importExternalFileContent must beNone
      }

      "write 'pass' to third party file if left empty" in new depsToDeleteCtx {
        writeArtifactsAsRootDependencies(someArtifactGroup1)

        writer.writeDependencies(Set(), Set(), Set(), localDepsToDelete = Set(someArtifactGroup1))

        localWorkspace.thirdPartyReposFileContent() must contain("pass")
      }
    }
  }

  private def writerFor(localWorkspace: BazelLocalWorkspace,
                        neverLinkResolver: NeverLinkResolver = NeverLinkResolver()) = {
    val statement = ImportExternalLoadStatement(importExternalRulePath = "@some_workspace//:import_external.bzl", importExternalMacroName = "some_import_external")
    new BazelDependenciesWriter(localWorkspace, neverLinkResolver, statement)
  }

  private def containsExactlyOneRuleOfName(name: String): Matcher[String] = (countMatches(s"""name += +"$name"""".r, _: String)) ^^ equalTo(1)

  private def containsIgnoringSpaces(target: String) = ((_: String).trimSpaces) ^^ contain(target.trimSpaces)

  private def countMatches(regex: Regex, string: String) = regex.findAllMatchIn(string).size

  private def containLoadStatementForGroupOf(coordinates: Coordinates, thirdPartyFolderPath: String = "third_party") = {
    val groupId = coordinates.groupIdForBazel

    contain(
      s"""load("//:$thirdPartyFolderPath/$groupId.bzl", ${groupId}_deps = "dependencies")""") and
      contain(s"${groupId}_deps()")
  }

  private def containMavenArtifact(coordinates: Coordinates) = {
    val Coordinates(groupId, artifactId, version, _, _) = coordinates
    containsIgnoringSpaces(
      s"""maven.artifact(group = "$groupId", artifact = "$artifactId", version = "$version")"""
    )
  }

  private def containRootScalaImportExternalRuleFor(coordinates: Coordinates, checksum: String = defaultChecksum, srcChecksum: String = defaultSrcChecksum) = {
    beSome(
      containsIgnoringSpaces(
        s"""|import_external(
            |    name = "${coordinates.workspaceRuleName}",
            |    artifact = "${coordinates.serialized}",
            |    artifact_sha256 = "$checksum",
            |    srcjar_sha256 = "$srcChecksum",
            |)""".stripMargin
      )
    )
  }

  private def containRootSnapshotScalaImportExternalRuleFor(coordinates: Coordinates, missingJar: Boolean) = {
    beSome(
      containsIgnoringSpaces(
        s"""|import_external(
            |    name = "${coordinates.workspaceRuleName}",
            |    artifact = "${coordinates.serialized}",${if (missingJar) "        # fixme: missing jar" else ""}
            |    snapshot_sources = 1,
            |)""".stripMargin
      )
    )
  }

  private def containSortedTargets: Matcher[Option[String]] = {
    val pattern = """name\s*?=\s*?"(.*)".*""".r

    def extractName(target: String) = {
      val firstLine = target.trim.split('\n')(0)
      val pattern(name) = firstLine
      name
    }

    beSome(
      beSorted[String] ^^ {
        (_: String).split("""scala_maven_import_external\(""").toSeq.drop(1).map(extractName)
      }
    )
  }

  private def containScalaImportExternalRuleFor(coordinates: Coordinates, withExtraParams: String) = {
    beSome(
      containsIgnoringSpaces(
        s"""|import_external(
            |    name = "${coordinates.workspaceRuleName}",
            |    artifact = "${coordinates.serialized}",
            |    artifact_sha256 = "$defaultChecksum",
            |    srcjar_sha256 = "$defaultSrcChecksum",
            |    $withExtraParams
            |)""".stripMargin
      )
    )
  }

  private def containMavenProtoRuleFor(coordinates: Coordinates) = {
    contain(
      s"""
         |    if native.existing_rule("${coordinates.workspaceRuleName}") == None:
         |        maven_proto(
         |            name = "${coordinates.workspaceRuleName}",
         |            artifact = "${coordinates.serialized}",
         |        )""".stripMargin)
  }

  implicit class StringExtended(string: String) {
    def trimSpaces = string.replaceAll(" +", " ").replaceAll("(?m)^ ", "")
  }

}
