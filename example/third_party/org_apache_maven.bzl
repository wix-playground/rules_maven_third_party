load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_maven_maven_artifact",
        artifact = "org.apache.maven:maven-artifact:3.5.4",
        artifact_sha256 = "6fbf25de86cce3afbaf5c502dff57df6d7c90cf9bec0ae0ffe5ab2467243c35b",
        srcjar_sha256 = "2c756760743d6e4c3a30e4288b35ad0192313d43cc02ae420ddba7ade13f7966",
        deps = [
            "@org_apache_commons_commons_lang3",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_artifact_manager",
        artifact = "org.apache.maven:maven-artifact-manager:2.2.1",
        artifact_sha256 = "d1e247c4ed3952385fd704ac9db2a222247cfe7d20508b4f3c76b90f857952ed",
        srcjar_sha256 = "cccb9d8731756a942d49b7f884a4b40b1d544bf7a29338b0d6dd1bc5bc9677ae",
        deps = [
            "@backport_util_concurrent_backport_util_concurrent",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_repository_metadata",
            "@org_apache_maven_wagon_wagon_provider_api",
            "@org_codehaus_plexus_plexus_container_default",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_builder_support",
        artifact = "org.apache.maven:maven-builder-support:3.5.4",
        artifact_sha256 = "43855ce29fc8001ef663a5bb2bb0473481b1f8f80cea7b3cc1d426af996960b2",
        srcjar_sha256 = "3d283bcfc1f73430e787c9d69caa94b848b874209eed2f07c5900c3af0de1a71",
        deps = [
            "@org_apache_commons_commons_lang3",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_core",
        artifact = "org.apache.maven:maven-core:2.2.1",
        artifact_sha256 = "cfdf0057b2d2a416d48b873afe5a2bf8d848aabbba07636149fcbb622c5952d7",
        srcjar_sha256 = "c98b654a798eb8baafabc43c84e0fc57081cb6d40e613684c85c611714251bd9",
        deps = [
            "@classworlds_classworlds",
            "@commons_cli_commons_cli",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_artifact_manager",
            "@org_apache_maven_maven_error_diagnostics",
            "@org_apache_maven_maven_model",
            "@org_apache_maven_maven_monitor",
            "@org_apache_maven_maven_plugin_api",
            "@org_apache_maven_maven_plugin_descriptor",
            "@org_apache_maven_maven_plugin_parameter_documenter",
            "@org_apache_maven_maven_profile",
            "@org_apache_maven_maven_project",
            "@org_apache_maven_maven_repository_metadata",
            "@org_apache_maven_maven_settings",
            "@org_apache_maven_reporting_maven_reporting_api",
            "@org_apache_maven_wagon_wagon_http",
            "@org_apache_maven_wagon_wagon_http_lightweight",
            "@org_apache_maven_wagon_wagon_provider_api",
            "@org_apache_maven_wagon_wagon_ssh",
            "@org_codehaus_plexus_plexus_container_default",
            "@org_codehaus_plexus_plexus_interactivity_api",
            "@org_codehaus_plexus_plexus_utils",
            "@org_slf4j_jcl_over_slf4j",
            "@org_slf4j_slf4j_jdk14",
            "@org_sonatype_plexus_plexus_sec_dispatcher",
        ],
        runtime_deps = [
            "@org_apache_maven_wagon_wagon_file",
            "@org_apache_maven_wagon_wagon_ssh_external",
            "@org_apache_maven_wagon_wagon_webdav_jackrabbit",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_error_diagnostics",
        artifact = "org.apache.maven:maven-error-diagnostics:2.2.1",
        artifact_sha256 = "b3005544708f8583e455c22b09a4940596a057108bccdadb9db4d8e048091fed",
        srcjar_sha256 = "392a83ef5fa2ae7c6861cfcd049989f193ad2bf793edcfd6b6a6bfa48519941a",
        deps = [
            "@org_codehaus_plexus_plexus_container_default",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_model",
        artifact = "org.apache.maven:maven-model:3.5.4",
        artifact_sha256 = "5ec1b94e9254c25480548633a48b7ae8a9ada7527e28f5c575943fe0c2ab7350",
        srcjar_sha256 = "5a52d14fe932024aed8848e2cd5217d6e8eb4176d014a9d75ab28a5c92c18169",
        deps = [
            "@org_apache_commons_commons_lang3",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_model_builder",
        artifact = "org.apache.maven:maven-model-builder:3.5.4",
        artifact_sha256 = "5dc10d69fd0a6e38f3ac3788bf1e63efd668af1fc23a08a2fdcffd85921d6f56",
        srcjar_sha256 = "c54b66772a2be78bf1f280627fb374745f82ffefbdeb5d6c45ee494a22af4197",
        deps = [
            "@com_google_guava_guava",
            "@org_apache_commons_commons_lang3",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_builder_support",
            "@org_apache_maven_maven_model",
            "@org_codehaus_plexus_plexus_component_annotations",
            "@org_codehaus_plexus_plexus_interpolation",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_monitor",
        artifact = "org.apache.maven:maven-monitor:2.2.1",
        artifact_sha256 = "a5f0d9e3f9afaa0cdc982e4f4c82d96a8608fd67c26f64eacd0405d5ac0f97d2",
        srcjar_sha256 = "3fe6c03379c16278d6250da5690bfd2b259ab7eba00310f816b2eed469ac99b8",
    )


    import_external(
        name = "org_apache_maven_maven_plugin_api",
        artifact = "org.apache.maven:maven-plugin-api:2.2.1",
        artifact_sha256 = "72a47a963563009c5e8b851491ced3f63e2d276b862bde1f9d10d53abac5b22f",
        srcjar_sha256 = "384488d4794ae505abbcc3e82a6fc3bb8d11eb50819b759c732399ca6d0b96b6",
    )


    import_external(
        name = "org_apache_maven_maven_plugin_descriptor",
        artifact = "org.apache.maven:maven-plugin-descriptor:2.2.1",
        artifact_sha256 = "ea41346759cb042027a4f6f98996427ba0ecf72602b1c3ee925461ddd00266b4",
        srcjar_sha256 = "b87dd2c2cf6e6bc49af130da2aeb2a3aac1d05fefddab8bb4f0faee9d634820b",
        deps = [
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_plugin_api",
            "@org_codehaus_plexus_plexus_container_default",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_plugin_parameter_documenter",
        artifact = "org.apache.maven:maven-plugin-parameter-documenter:2.2.1",
        artifact_sha256 = "d87645908695bd18375b77b9149741a9309521f9e13a872d5aa6bcdf361d0226",
        srcjar_sha256 = "6ba475ad4bc09c7c31db35e82b01591ab1abc25a1d619472cd9fbf976e763f3b",
        deps = [
            "@org_codehaus_plexus_plexus_container_default",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_plugin_registry",
        artifact = "org.apache.maven:maven-plugin-registry:2.2.1",
        artifact_sha256 = "4ad0673155d7e0e5cf6d13689802d8d507f38e5ea00a6d2fb92aef206108213d",
        srcjar_sha256 = "2e818a80b12502188a06f650ec44e02b8716d6dde349b4bf66acde9d3653ccf1",
        deps = [
            "@org_codehaus_plexus_plexus_container_default",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_profile",
        artifact = "org.apache.maven:maven-profile:2.2.1",
        artifact_sha256 = "ecaffef655fea6b138f0855a12f7dbb59fc0d6bffb5c1bfd31803cccb49ea08c",
        srcjar_sha256 = "f24d1d7c170427b42fc3484b421d83907c40a7aac3e7a85e2ee5097e36264b91",
        deps = [
            "@org_apache_maven_maven_model",
            "@org_codehaus_plexus_plexus_container_default",
            "@org_codehaus_plexus_plexus_interpolation",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_project",
        artifact = "org.apache.maven:maven-project:2.2.1",
        artifact_sha256 = "24ddb65b7a6c3befb6267ce5f739f237c84eba99389265c30df67c3dd8396a40",
        srcjar_sha256 = "e4ee161d2ac2f1fb5680f1317fae243691feb6d607a22b6fefb1a04570c11801",
        deps = [
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_artifact_manager",
            "@org_apache_maven_maven_model",
            "@org_apache_maven_maven_plugin_registry",
            "@org_apache_maven_maven_profile",
            "@org_apache_maven_maven_settings",
            "@org_codehaus_plexus_plexus_container_default",
            "@org_codehaus_plexus_plexus_interpolation",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_repository_metadata",
        artifact = "org.apache.maven:maven-repository-metadata:3.0.3",
        artifact_sha256 = "5cc35e32e4475a1e0e5a36fac8dd45fd55c43f5add0dba81be8a76b02d4c8757",
        srcjar_sha256 = "a10077b36d66c60d5fd69af726453bcc3e1ce343114f009319a95cfef6fee83b",
        deps = [
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_resolver_provider",
        artifact = "org.apache.maven:maven-resolver-provider:3.5.4",
        artifact_sha256 = "3a5d15bf994da32621a3beabe76f8a611bf92b6a1e42a43d827ff5a3d94851c4",
        srcjar_sha256 = "3174c174d35da70b92f0fc983c6fd92e1617a77feafc1cb833674f14897f792a",
        deps = [
            "@javax_inject_javax_inject",
            "@org_apache_commons_commons_lang3",
            "@org_apache_maven_maven_model",
            "@org_apache_maven_maven_model_builder",
            "@org_apache_maven_maven_repository_metadata",
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_impl",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_apache_maven_resolver_maven_resolver_util",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_settings",
        artifact = "org.apache.maven:maven-settings:2.2.1",
        artifact_sha256 = "9a9f556713a404e770c9dbdaed7eb086078014c989291960c76fdde6db4192f7",
        srcjar_sha256 = "1476e996a31d91c733d48794fc3c4a193ed9363e7a633e0df959b3b3b97bfd28",
        deps = [
            "@org_apache_maven_maven_model",
            "@org_codehaus_plexus_plexus_container_default",
            "@org_codehaus_plexus_plexus_interpolation",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )
