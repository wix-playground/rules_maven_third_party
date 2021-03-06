package com.wix.build.maven

import com.wix.build.maven.resolver.aether.AetherMavenDependencyResolver
import org.specs2.specification.BeforeAfterAll

trait FakeMavenRepositorySupport {
  self: BeforeAfterAll =>

  val fakeMavenRepository = new FakeMavenRepository()
  val aetherMavenDependencyResolver = new AetherMavenDependencyResolver(List(fakeMavenRepository.url))

  override def beforeAll(): Unit = fakeMavenRepository.start()

  override def afterAll(): Unit = fakeMavenRepository.stop()
}
