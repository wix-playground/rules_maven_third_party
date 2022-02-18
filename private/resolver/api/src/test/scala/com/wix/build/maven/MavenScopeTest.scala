package com.wix.build.maven

import com.wix.build.maven.mapper.{Mapper, TypeAddingMixin}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.core.{Fragment, Fragments}

class MavenScopeTest extends SpecWithJUnit {
  val mapper = Mapper.mapper.addMixIn(classOf[MavenScope], classOf[TypeAddingMixin])

  val ScopesToNames = List(
    ScopeToName(MavenScope.Compile, "compile"),
    ScopeToName(MavenScope.Test, "test"),
    ScopeToName(MavenScope.Runtime, "runtime"),
    ScopeToName(MavenScope.Provided, "provided"),
    ScopeToName(MavenScope.System, "system")
  )

  private def aNewInstanceOf(scope: MavenScope): MavenScope = {
    mapper.readValue(mapper.writeValueAsString(scope), classOf[MavenScope])
  }

  private def extractTest(scopeToName: ScopeToName): Fragment = {
    s"parse ${scopeToName.scope} from string '${scopeToName.name}'" in {
      MavenScope.of(scopeToName.name) mustEqual scopeToName.scope
    }

    s"have equals working on different instances(!) of the same ${scopeToName.scope} value " +
      "(different instances can be created by jackson deserialization)" in {
      val differentInstance = aNewInstanceOf(scopeToName.scope)
      differentInstance.eq(scopeToName.scope) must beFalse
      differentInstance mustEqual scopeToName.scope
    }

    s"have hash working on different instances(!) of the same ${scopeToName.scope} value " +
      "(different instances can be created by jackson deserialization)" in {
      val differentInstance = aNewInstanceOf(scopeToName.scope)
      differentInstance.eq(scopeToName.scope) must beFalse
      differentInstance.hashCode() mustEqual scopeToName.scope.hashCode()
    }
  }

  def allTests: Fragments = Fragments(ScopesToNames.map(extractTest): _*)

  "MavenScope" should {
    allTests
  }

  "equals" should {
    "return 'false' for two different scopes" in {
      MavenScope.Compile mustNotEqual MavenScope.Provided
    }

    "return 'false' when comparing to an object which is not an instance of MavenScope" in {
      MavenScope.System mustNotEqual 3
    }
  }

  "hashCode" should {
    "return different hash for different scopes" in {
      MavenScope.Runtime.hashCode() mustNotEqual MavenScope.Test.hashCode()
    }
  }

}

case class ScopeToName(scope: MavenScope, name: String)
