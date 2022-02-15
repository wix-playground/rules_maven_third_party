package com.wix.build.maven

class PropertyNotDefinedException(dependency: Dependency)
  extends RuntimeException(s"Cannot evaluate all tokens in ${dependency.coordinates}")
