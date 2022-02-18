package com.wix.build.bazel

import com.wix.build.maven.MavenMakers._
import com.wix.build.maven._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecificationWithJUnit

//noinspection TypeAnnotation
class AnnotatedDepNodeTransformerTest extends SpecificationWithJUnit {

  val artifact = someCoordinates("some-artifact")
  val transitiveDep = someCoordinates("some-transitiveDep")
  val transitiveDeps = Set(transitiveDep)
  val runtimeDepNode = BazelDependencyNode(asCompileDependency(artifact), Set(asRuntimeDependency(transitiveDep)))
  val compileTimeDepNode = BazelDependencyNode(asCompileDependency(artifact), Set(asCompileDependency(transitiveDep)))
  val thirdPartyPath = "third_party"

  "AnnotatedDependencyNodeTransformer" should {
    "return import external rule with 'linkable' transitive runtime dep if came from global overrided list and is missing from local neverlink " in {
      val transformer = new AnnotatedDependencyNodeTransformer(new NeverLinkResolver(transitiveDeps, localNeverlinkDependencies = Set()), thirdPartyPath)

      transformer.annotate(runtimeDepNode) must beAnnotatedDependencyNode(
        anArtifact = be_===(artifact),
        runtimeDeps = contain(ImportExternalDep(transitiveDep, linkableSuffixNeeded = true)),
        compileDeps = beEmpty)
    }

    "return import external rule with 'linkable' transitive compile dep if came from global overrided list and is missing from local neverlink " in {
      val transformer = new AnnotatedDependencyNodeTransformer(new NeverLinkResolver(transitiveDeps, localNeverlinkDependencies = Set()), thirdPartyPath)

      transformer.annotate(compileTimeDepNode) must beAnnotatedDependencyNode(
        anArtifact = be_===(artifact),
        runtimeDeps = beEmpty,
        compileDeps = contain(ImportExternalDep(transitiveDep, linkableSuffixNeeded = true))
      )
    }

    "return import external rule with transitive runtime dep without 'linkable' suffix if it can be found both on global and local neverlink lists" in {
      val transformer = new AnnotatedDependencyNodeTransformer(new NeverLinkResolver(transitiveDeps, localNeverlinkDependencies = transitiveDeps), thirdPartyPath)

      transformer.annotate(runtimeDepNode) must beAnnotatedDependencyNode(
        anArtifact = be_===(artifact),
        runtimeDeps = contain(ImportExternalDep(transitiveDep, linkableSuffixNeeded = false)),
        compileDeps = beEmpty
      )
    }

    "return import external rule with transitive compiletime dep without 'linkable' suffix if it can be found both on global and local neverlink lists" in {
      val transformer = new AnnotatedDependencyNodeTransformer(new NeverLinkResolver(transitiveDeps, localNeverlinkDependencies = transitiveDeps), thirdPartyPath)

      transformer.annotate(compileTimeDepNode) must beAnnotatedDependencyNode(
        anArtifact = be_===(artifact),
        runtimeDeps = beEmpty,
        compileDeps = contain(ImportExternalDep(transitiveDep, linkableSuffixNeeded = false))
      )
    }
  }

  def beAnnotatedDependencyNode(anArtifact: Matcher[Coordinates] = AlwaysMatcher[Coordinates](),
                                runtimeDeps: Matcher[Set[BazelDep]] = AlwaysMatcher[Set[BazelDep]](),
                                compileDeps: Matcher[Set[BazelDep]] = AlwaysMatcher[Set[BazelDep]]()
                               ): Matcher[AnnotatedDependencyNode] =
    anArtifact ^^ {
      (_: AnnotatedDependencyNode).baseDependency.coordinates aka "artifact"
    } and runtimeDeps ^^ {
      (_: AnnotatedDependencyNode).runtimeDependencies aka "runtimeDeps"
    } and compileDeps ^^ {
      (_: AnnotatedDependencyNode).compileTimeDependencies aka "compileTimeDeps"
    }
}
