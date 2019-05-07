package com.wix.build.maven

import com.wix.build.maven.dependency.resolver.api.v1.DependenciesClosureRequest
import com.wix.hoopoe.ids.Guid

case class CalculateClosureTask(jobId: Guid[CalculateClosureTask], request: DependenciesClosureRequest)
