package com.wix.build.sync.e2e

import com.wix.build.sync.e2e.WireMockTestSupport.wireMockServer
import com.wix.framework.test.env.ManagedService

class WireMockManagedService extends ManagedService {
  override def start(): Unit =   wireMockServer.start()

  override def stop(): Unit = wireMockServer.stop()

  def alwaysReturnSha256Checksums() = {
    WireMockTestSupport.alwaysReturnSha256Checksums()
  }
}
