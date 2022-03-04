load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_maven_maven_artifact",
        artifact = "org.apache.maven:maven-artifact:3.8.4",
        artifact_sha256 = "4273b4e84805f7350eb61a1eea5debfd71d1147414b3b441b92d535218cdf0ae",
        srcjar_sha256 = "3cf79459a13160b837a6176271603e5b1647d0cf1e53a018a14104c5b0b3502f",
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
        artifact = "org.apache.maven:maven-builder-support:3.8.4",
        artifact_sha256 = "b64161e6ffd30782d97c205942bba219d60c53a8f4442e69abdfd428d7691135",
        srcjar_sha256 = "cfa6acd0014b10dcd70e99ddeb4dd8448d1356786c90c899066d2878f976d9f1",
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
        artifact = "org.apache.maven:maven-model:3.8.4",
        artifact_sha256 = "91ec0d6d564a12483e1569b0ef72ff3d9e921c5ba07201fa7ab9c7694db8844a",
        srcjar_sha256 = "638caaf522d9629b1c3df3e7ea7e51427f9512495a797efc4716d0c9353507cd",
        deps = [
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_model_builder",
        artifact = "org.apache.maven:maven-model-builder:3.8.4",
        artifact_sha256 = "8d0ed4b5cc5c06610f97935982458260165cb7e57c781ca7c9ef8b6e01ce1456",
        srcjar_sha256 = "ac9ffa14c57cb95b3b84a17b2cbbc90a315cc419edd584fbd5afdf4ea89620cb",
        deps = [
            "@javax_inject_javax_inject",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_builder_support",
            "@org_apache_maven_maven_model",
            "@org_codehaus_plexus_plexus_interpolation",
            "@org_codehaus_plexus_plexus_utils",
            "@org_eclipse_sisu_org_eclipse_sisu_inject",
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
        artifact = "org.apache.maven:maven-repository-metadata:3.8.4",
        artifact_sha256 = "62a97989068af34eef374bedcca120a1c2b0bd5a2d48460d306944084cc495f9",
        srcjar_sha256 = "8ada6fc9d893559655b44f0509e2a42e42ee75b167e787ee1bb523963e28d01f",
        deps = [
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_maven_resolver_provider",
        artifact = "org.apache.maven:maven-resolver-provider:3.8.4",
        artifact_sha256 = "046c7d1635f91283b4f7a41b579953857914c7e0d96545b557491537b327e156",
        srcjar_sha256 = "07a0e9709661b72031a96e2bcd1212aa72b1e669080d9030bbc29c7cad7a61a1",
        deps = [
            "@javax_inject_javax_inject",
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
