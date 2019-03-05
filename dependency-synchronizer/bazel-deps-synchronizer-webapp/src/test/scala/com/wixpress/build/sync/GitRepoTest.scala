package com.wix.build.sync

import org.specs2.mutable.SpecificationWithJUnit

class GitRepoTest extends SpecificationWithJUnit {
  "GitRepo" should {
    "parse org and repo from git url" in {
      val gitRepo = GitRepo(s"https://github.com/wix-private-like/core-server-build-tools-like.git")

      gitRepo.org must_=== ("wix-private-like")
      gitRepo.repoName must_=== ("core-server-build-tools-like")
    }

    "use defaults if url is not uri" in {
      val gitRepo = GitRepo(s"some-weird-string")

      gitRepo.org must_===("defaultForNonValidUrl-some-weird-string")
      gitRepo.repoName must_===("defaultForNonValidUrl-some-weird-string")
    }

    "use explicitly supplied org and repo" in {
      val gitRepo = GitRepo("someUrl", "someOrg", "someRepoName")

      gitRepo.org must_===("someOrg")
      gitRepo.repoName must_===("someRepoName")
    }
  }
}
