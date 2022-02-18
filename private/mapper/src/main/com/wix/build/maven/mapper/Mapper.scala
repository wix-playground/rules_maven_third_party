package com.wix.build.maven.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.util.TimeZone

object Mapper {
  private val defaultModules = Seq(new DefaultScalaModule)

  val mapper: ObjectMapper = new ObjectMapper()
    .registerModules(defaultModules: _*)
    .setTimeZone(TimeZone.getTimeZone("UTC"))

}
