package com.wix.build.sync.e2e


import better.files.File
import com.wix.build.bazel._
import com.wix.build.maven._
import com.wix.build.sync.BazelMavenManagedDepsSynchronizer

object TestApp extends App {
  
  def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis()
    println("Elapsed time: " + (t1 - t0)/1000 + "s")
    result
  }
  val local = File("/Users/ors/workspace/server/wix-framework")
  val bazelRepo = new MockBazelRepository(local)
  val managedArtifact = Coordinates.deserialize("com.wix.fake:third-party-dependencies:pom:100.0.0-SNAPSHOT")
  val resolver = new AetherMavenDependencyResolver(List(
    "http://repo.example.com:80/artifactory/libs-releases",
    "http://repo.example.com:80/artifactory/libs-snapshots"))

  val s = new BazelMavenManagedDepsSynchronizer(resolver,bazelRepo, _ => None,
    ImportExternalLoadStatement(importExternalRulePath = "@some_workspace//:import_external.bzl", importExternalMacroName = "some_import_external"))
  val dep = Dependency(Coordinates("com.wix","wix-meta-site-manager-api","2.183.0-SNAPSHOT",Packaging("jar"),Some("tests")),MavenScope.Compile)
  val deps = resolver.dependencyClosureOf(List(dep),List.empty)
  println(deps)

}

class MockBazelRepository(local: File) extends BazelRepository {

  override def resetAndCheckoutMaster(): BazelLocalWorkspace = new FileSystemBazelLocalWorkspace(local)

  override def persist(branchName: String, message: String): Unit = {
    println(s"persisting to branch $branchName")
    println(s"""message:"$message"""")
  }

  override def repoPath: String = ""
}
