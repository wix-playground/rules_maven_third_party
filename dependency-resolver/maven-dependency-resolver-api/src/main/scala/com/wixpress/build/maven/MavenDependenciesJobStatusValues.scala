package com.wix.build.maven

sealed abstract class JobStatus(val status: String)

case object JobNotFound extends JobStatus("Not Found")
case object Completed extends JobStatus("Completed")

