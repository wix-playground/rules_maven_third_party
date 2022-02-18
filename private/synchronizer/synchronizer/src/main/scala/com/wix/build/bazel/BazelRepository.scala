package com.wix.build.bazel

trait BazelRepository {

  def resetAndCheckoutMaster(): BazelLocalWorkspace

  def persist(branchName: String, message: String): Unit

  def repoPath: String
}
