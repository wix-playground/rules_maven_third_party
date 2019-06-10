package com.wix.build.sync

import com.wix.framework.async.WixExecutors
import org.springframework.web.bind.annotation._

import scala.concurrent.{ExecutionContextExecutorService, Future}

@RestController
@RequestMapping(Array("/api"))
class ManualSyncController(managedDependenciesUpdate: ManagedDependenciesUpdateHandler) {
  implicit val ec: ExecutionContextExecutorService =
    WixExecutors.newExecutor(4).withName("synchronizer").buildExecutionContext()

  @RequestMapping(value = Array("/sync"), method = Array(RequestMethod.GET))
  //useful for testing behaviour without relying on an actual teamcity build, i.e no more dummy commits
  def syncToBranch(@RequestParam("branch") branchName: String): Unit = Future(managedDependenciesUpdate.run(branchName))

}
