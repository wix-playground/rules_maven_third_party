package com.wix.build.sync

import com.wix.build.maven._

object DependenciesRemoteStorageTestSupport {
  def remoteStorageWillReturn(checksum: Option[String] = None, srcChecksum: Option[String] = None): DependenciesRemoteStorage = {
    new DependenciesRemoteStorage {
      override def checksumFor(node: DependencyNode): Option[String] =
        if (node.baseDependency.coordinates.classifier.contains("sources")) srcChecksum else checksum
    }
  }
}
