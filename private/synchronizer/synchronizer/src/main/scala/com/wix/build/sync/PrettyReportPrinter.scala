package com.wix.build.sync

import org.slf4j.LoggerFactory

object PrettyReportPrinter {
  private val headersLog = LoggerFactory.getLogger("pretty-print-headers")
  private val defaultColorLog = LoggerFactory.getLogger("pretty-print-default-text")

  def printReport(report: UserAddedDepsConflictReport): Unit = {
    headersLog.info("\n====== UPDATE REPORT ========")
    if (report.higherVersionConflicts.isEmpty && report.differentManagedVersionConflicts.isEmpty)
      defaultColorLog.info("no conflicting changes made")
    if (report.differentManagedVersionConflicts.nonEmpty) {
      headersLog.info("==== =    NOTEWORTHY CHANGES TO MANAGED ARTIFACTS =====")
      defaultColorLog.info("managed artifacts can be found in https://github.com/wix-private/core-server-build-tools/tree/master/third_party")
      defaultColorLog.info(report.differentManagedVersionConflicts.mkString("\n"))
    }
    if (report.higherVersionConflicts.nonEmpty) {
      headersLog.info("====== NOTEWORTHY CHANGES TO LOCAL ARTIFACTS ======")
      defaultColorLog.info(report.higherVersionConflicts.mkString("\n"))
    }
    headersLog.info("!!! PLEASE, review changed and added third_party files in the target repo !!!")
    headersLog.info("run this - 'git status | grep third_party/' to make sure you don't miss new unstaged files!")
  }
}
