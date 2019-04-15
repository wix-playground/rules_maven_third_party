package com.wix.build.sync.migrate

import java.nio.file.Files

import better.files.File
import com.wix.build.BazelWorkspaceDriver
import com.wix.build.bazel.FileSystemBazelLocalWorkspace
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

//noinspection TypeAnnotation
class MigrateToSnapshotSourcesMacroCliE2E extends SpecWithJUnit {
  sequential

  "MigrateToSnapshotSourcesMacroCli" should {

    "not change import_external target for non hybrid file" in new basicCtx {
      targetRepoWorkspace.overwriteThirdPartyImportTargetsFile("com_wix_metasite", import_external_text)

      runSnapshotsToSingleRepoSynchronizerCli()

      targetRepoWorkspace.thirdPartyImportTargetsFileContent("com_wix_metasite") must beSome(import_external_text)
    }

    "migrate import_external_no_src" in new basicCtx {
      targetRepoWorkspace.overwriteThirdPartyImportTargetsFile("com_wix", import_no_src_orig_text)

      runSnapshotsToSingleRepoSynchronizerCli()

      targetRepoWorkspace.thirdPartyImportTargetsFileContent("com_wix") must beSome(import_no_src_new_text)
    }

  }

  trait basicCtx extends Scope {

    val targetRepoPath = File(Files.createTempDirectory("target-repo"))
    val targetRepoWorkspace = new FileSystemBazelLocalWorkspace(targetRepoPath)
    val targetRepo = new BazelWorkspaceDriver(targetRepoWorkspace)

    private def argsWithSnapshotsToSync() = Array(targetRepoPath.toString)

    def runSnapshotsToSingleRepoSynchronizerCli(): Unit = {
      MigrateToSnapshotSourcesMacroCli.main(argsWithSnapshotsToSync())
    }

    val import_external_text = s"""load("@core_server_build_tools//:import_external.bzl", import_external = "safe_wix_scala_maven_import_external")
                                  |
                                  |def dependencies():
                                  |
                                  |  import_external(
                                  |      name = "com_wix_metasite_laboratory_support",
                                  |      artifact = "com.wix.metasite:laboratory-support:1.0.0-SNAPSHOT",
                                  |      deps = [
                                  |          "@com_wix_common_wix_laboratory_api",
                                  |          "@org_scala_lang_scala_library"
                                  |      ],
                                  |  )
                                  |
                                  |
                                  |  import_external(
                                  |      name = "com_wix_metasite_msm_authorization_client_api",
                                  |      artifact = "com.wix.metasite:msm-authorization-client-api:1.0.0-SNAPSHOT",
                                  |      deps = [
                                  |          "@com_wix_common_wix_framework_api_objects_domain",
                                  |          "@com_wix_hoopoe_hoopoe_data_structures",
                                  |          "@org_scala_lang_scala_library"
                                  |      ],
                                  |  )""".stripMargin

    val import_no_src_orig_text = s"""load("@core_server_build_tools//:import_external.bzl", import_external = "wix_snapshot_scala_maven_import_external")
                                  |load("@core_server_build_tools//:import_external.bzl", import_external_no_src = "safe_wix_scala_maven_import_external")
                                  |
                                  |def dependencies():
                                  |
                                  |  import_external_no_src(
                                  |      name = "com_wix_nutri_matic",
                                  |      artifact = "com.wix:nutri-matic:0.3.0",
                                  |      jar_sha256 = "b6ae19a74777ef90588603571465a13a89db20152aa3105bc878189d35a9e2ba",
                                  |  )
                                  |
                                  |  import_external(
                                  |      name = "com_wix_some_snap",
                                  |      artifact = "com.wix:some-snap:0.7.0-SNAPSHOT",
                                  |      jar_sha256 = "b6ae19a74777ef90588603571465a13a89db20152aa3105bc878189d35a9e2ba",
                                  |  )
                                  |""".stripMargin

    val import_no_src_new_text = s"""load("@core_server_build_tools//:import_external.bzl", import_external = "safe_wix_scala_maven_import_external")
                                |
                                |def dependencies():
                                |
                                |  import_external(
                                |      name = "com_wix_nutri_matic",
                                |      artifact = "com.wix:nutri-matic:0.3.0",
                                |      jar_sha256 = "b6ae19a74777ef90588603571465a13a89db20152aa3105bc878189d35a9e2ba",
                                |  )
                                |
                                |  import_external(
                                |      name = "com_wix_some_snap",
                                |      artifact = "com.wix:some-snap:0.7.0-SNAPSHOT",
                                |      jar_sha256 = "b6ae19a74777ef90588603571465a13a89db20152aa3105bc878189d35a9e2ba",
                                |      snapshot_sources = 1,
                                |  )
                                |""".stripMargin
  }

}
