package com.wix.build.bazel

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


class InMemoryBazelRepository(bazelLocalWorkspace: BazelLocalWorkspace) extends BazelRepository {

  private val branchToChangeLog = mutable.HashMap.empty[String, ListBuffer[Change]].withDefaultValue(ListBuffer.empty)

  override def resetAndCheckoutMaster(): BazelLocalWorkspace = bazelLocalWorkspace

  override def persist(branchName: String, message: String): Unit = {
    val changeLog = branchToChangeLog(branchName)
    changeLog += Change(message)
    branchToChangeLog.put(branchName, changeLog)
  }

  def allChangesInBranch(branchName: String): List[Change] = branchToChangeLog(branchName).toList

  override def repoPath: String = ""
}

case class Change(message: String)