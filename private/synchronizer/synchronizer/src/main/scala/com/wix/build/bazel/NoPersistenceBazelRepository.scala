package com.wix.build.bazel

import better.files.File

//TODO: share this among instances
class NoPersistenceBazelRepository(local: File, thirdPartyDestination: String) extends BazelRepository {

  override def resetAndCheckoutMaster(): BazelLocalWorkspace = new FileSystemBazelLocalWorkspace(local, new ThirdPartyPaths(thirdPartyDestination))

  override def persist(branchName: String, message: String): Unit = ()

  override def repoPath: String = local.pathAsString
}
