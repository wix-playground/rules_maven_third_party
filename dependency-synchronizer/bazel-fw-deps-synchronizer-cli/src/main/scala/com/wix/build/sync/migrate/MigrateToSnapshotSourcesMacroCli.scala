package com.wix.build.sync.migrate

import better.files.File
import com.wix.build.bazel._
import org.slf4j.LoggerFactory

object MigrateToSnapshotSourcesMacroCli extends App {

  private val log = LoggerFactory.getLogger(getClass)

  val config = MigrateToSnapshotSourcesMacroCliConfig.parse(args)

  val targetRepoLocalClone = config.targetRepoUrl
  log.info("targetRepoLocalClone: " + targetRepoLocalClone)

  val targetBazelRepo: BazelRepository = new NoPersistenceBazelRepository(File(targetRepoLocalClone))

  val thirdPartyImportFiles: Map[File, String] = targetBazelRepo.localWorkspace().allThirdPartyImportTargetsFiles()
  for ((file, content) <- thirdPartyImportFiles ) {

    val snapshotHeaderExists = ImportExternalTargetsFileReader.wixSnapshotHeaderExists(content)

    if (snapshotHeaderExists) {

      val targets = ImportExternalTargetsFileReader.splitToStringsWithJarImportsInside(content)
      targets foreach { target =>
        val rule = ImportExternalTargetsFileReader(content).parseTargetTextAndName(target).get

        val ruleToPersist = {
          if (target.contains("import_external_no_src") || !rule.artifact.contains("-SNAPSHOT"))
            rule.copy(snapshotSources = false)
          else
            rule.copy(snapshotSources = true)
        }

        ImportExternalTargetsFile.persistTargetAndCleanHeaders(ruleToPersist, targetBazelRepo.localWorkspace())
      }
    }
  }

}

case class MigrateToSnapshotSourcesMacroCliConfig(targetRepoUrl: String)

object MigrateToSnapshotSourcesMacroCliConfig {
  def parse(args: Array[String]): MigrateToSnapshotSourcesMacroCliConfig = args match {
    case Array(targetBazelRepo) => MigrateToSnapshotSourcesMacroCliConfig(targetBazelRepo)
    case _ => throw new IllegalArgumentException()
  }
}


