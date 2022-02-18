package com.wix.build.bazel

class BazelDependenciesPersister(commitHeader: String, bazelRepository: BazelRepository) {

  def persistWithMessage(branchName: Option[String] = None, asPr: Boolean = false): Unit = {
    val branch = branchName.getOrElse("master")
    val message = finalMsg(asPr)

    bazelRepository.persist(branch, message)
  }

  private def finalMsg(asPr: Boolean): String = {

    val msg = s"""$commitHeader
       |""".stripMargin

    asPr match {
      case true => msg + "#pr"
      case _ => msg
    }
  }

}
