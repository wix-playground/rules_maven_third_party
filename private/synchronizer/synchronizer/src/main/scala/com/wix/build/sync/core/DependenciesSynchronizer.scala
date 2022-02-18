package com.wix.build.sync.core

import com.wix.build.maven._

trait DependenciesSynchronizer {
  def sync(dependencies: List[Dependency]): Unit
}
