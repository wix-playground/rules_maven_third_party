package com.wix.build.bazel

import org.specs2.mutable.SpecWithJUnit

class DestinationPackageTest extends SpecWithJUnit {
  "resolves single element package" in {
    DestinationPackage.resolveFromDestination("third_party") mustEqual DestinationPackage("//:third_party")
  }

  "resolves multiple elements packages" in {
    DestinationPackage.resolveFromDestination("some/where/deep") mustEqual DestinationPackage("//some/where:deep")
  }
}
