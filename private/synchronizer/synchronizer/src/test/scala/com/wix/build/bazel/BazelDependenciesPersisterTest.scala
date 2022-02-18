package com.wix.build.bazel

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class BazelDependenciesPersisterTest extends SpecificationWithJUnit {
  "BazelDependenciesPersister should" >> {
    trait ctx extends Scope {
      val branch = "master"
      val header = "some header"
      val bazelRepository = new FakeBazelRepository()
      val persister = new BazelDependenciesPersister(header, bazelRepository)
    }

    "call persister with requested branch" in new ctx {
      persister.persistWithMessage(branchName = Option("blah"))

      bazelRepository.lastCommit should beEqualTo(DummyCommit(
        branchName = "blah",
        message =
          s"""$header
             |""".stripMargin))
    }

    "call persister with master as default branch" in new ctx {
      persister.persistWithMessage()

      bazelRepository.lastCommit should beEqualTo(DummyCommit(
        branchName = "master",
        message =
          s"""$header
             |""".stripMargin))
    }

    "given asPr = true add #pr" in new ctx {
      persister.persistWithMessage(asPr = true)

      bazelRepository.lastCommit should beEqualTo(DummyCommit(
        branchName = "master",
        message =
          s"""$header
             |#pr""".stripMargin))
    }

  }
}

class FakeBazelRepository() extends BazelRepository {
  private val commits = collection.mutable.ListBuffer.empty[DummyCommit]

  def lastCommit: DummyCommit = commits.last

  override def resetAndCheckoutMaster(): BazelLocalWorkspace = {
    throw new RuntimeException("this class is only for dummy commits purpose")
  }

  override def persist(branchName: String, message: String): Unit = {
    commits.append(DummyCommit(branchName, message))
  }

  override def repoPath: String = ""
}

case class DummyCommit(branchName: String, message: String)
