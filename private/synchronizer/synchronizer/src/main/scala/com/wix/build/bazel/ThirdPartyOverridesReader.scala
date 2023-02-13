package com.wix.build.bazel

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object ThirdPartyOverridesReader {

  def from(json: String): ThirdPartyOverrides = {
    mapper.readValue(json, classOf[ThirdPartyOverrides])
  }

  def mapper: ObjectMapper = {
    val objectMapper = new ObjectMapper()
      .registerModule(DefaultScalaModule)
    objectMapper.registerModule(overrideCoordinatesKeyModule())
    objectMapper
  }

  private def overrideCoordinatesKeyModule(): Module =
    new SimpleModule()
      .addKeyDeserializer(classOf[OverrideCoordinates], new OverrideCoordinatesKeyDeserializer())
      .addKeySerializer(classOf[OverrideCoordinates], new OverrideCoordinatesKeySerializer())

  private class OverrideCoordinatesKeySerializer() extends JsonSerializer[OverrideCoordinates] {
    override def serialize(value: OverrideCoordinates, gen: JsonGenerator, serializers: SerializerProvider): Unit =
      gen.writeFieldName(value.groupId + ":" + value.artifactId)
  }

  private class OverrideCoordinatesKeyDeserializer() extends KeyDeserializer {
    override def deserializeKey(key: String, ctxt: DeserializationContext): AnyRef =
      key.split(':') match {
        case Array(groupId, artifactId) => OverrideCoordinates(groupId, artifactId)
        case _ => throw new IllegalArgumentException(s"OverrideCoordinates key should be in form of groupId:artifactId, got $key")
      }
  }

}
