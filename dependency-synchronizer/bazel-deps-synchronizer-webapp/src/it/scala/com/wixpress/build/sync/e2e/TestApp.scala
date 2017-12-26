package com.wix.build.sync.e2e


import better.files.File
import com.wix.build.bazel.{BazelLocalWorkspace, BazelRepository, FileSystemBazelLocalWorkspace}
import com.wix.build.maven.{AetherMavenDependencyResolver, Coordinates, Dependency, MavenScope}
import com.wix.build.sync.BazelMavenSynchronizer

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

  val s = new BazelMavenSynchronizer(resolver,bazelRepo)
  val dep = Dependency(Coordinates("com.wix","wix-meta-site-manager-api","2.183.0-SNAPSHOT",Some("jar"),Some("tests")),MavenScope.Compile)
  val deps = resolver.dependencyClosureOf(Set(dep),Set.empty)
  println(deps)

}

class MockBazelRepository(local: File) extends BazelRepository {

  override def localWorkspace(branchName: String): BazelLocalWorkspace = new FileSystemBazelLocalWorkspace(local)

  override def persist(branchName: String, changedFilePaths: Set[String], message: String): Unit = {
    println(s"persisting to branch $branchName files $changedFilePaths")
    println(s"changed files:")
    changedFilePaths.foreach(file => println(s"- $file"))
    println(s"""message:"$message"""")
  }
}
