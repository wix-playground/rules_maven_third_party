package com.wix.build.bazel

import com.wix.build.bazel.LibraryRuleDep.nonJarLabelBy
import com.wix.build.maven.Coordinates

trait BazelDep {
  val coordinates: Coordinates

  def toLabel: String

  def toVersionedLabel: String
}

case class ImportExternalDep(coordinates: Coordinates, linkableSuffixNeeded: Boolean = false) extends BazelDep {
  override def toLabel: String = ImportExternalRule.jarLabelBy(coordinates, linkableSuffixNeeded)
  override def toVersionedLabel: String = ImportExternalRule.jarLabelWithVersion(coordinates, linkableSuffixNeeded)
}

// TODO: add workspace name....
case class LibraryRuleDep(coordinates: Coordinates, thirdPartyPath: String) extends BazelDep {
  override def toLabel: String = nonJarLabelBy(coordinates, thirdPartyPath)
  override def toVersionedLabel: String = nonJarLabelBy(coordinates, thirdPartyPath)
}

object LibraryRuleDep {
  def nonJarLabelBy(coordinates: Coordinates, thirdPartyPath: String): String = {
    s"@${LibraryRule.nonJarLabelBy(coordinates, thirdPartyPath)}"
  }

  def apply(coordinates: Coordinates, thirdPartyPath: String): LibraryRuleDep = {
    new LibraryRuleDep(coordinates, thirdPartyPath)
  }
}