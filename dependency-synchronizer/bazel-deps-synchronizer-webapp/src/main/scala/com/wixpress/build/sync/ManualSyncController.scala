package com.wix.build.sync

import com.wix.framework.async.WixExecutors
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestMethod, RestController}

import scala.concurrent.{ExecutionContextExecutorService, Future}

@RestController
@RequestMapping(Array("/api"))
class ManualSyncController(managedDependenciesUpdate: ManagedDependenciesUpdateHandler) {
  implicit val ec: ExecutionContextExecutorService =
    WixExecutors.newExecutor(4).withName("synchronizer").buildExecutionContext()

  @RequestMapping(value = Array("/sync"), method = Array(RequestMethod.POST))
  def syncToBranch(@PathVariable("branch") branchName: String): Unit = Future(managedDependenciesUpdate.run(branchName))

}
