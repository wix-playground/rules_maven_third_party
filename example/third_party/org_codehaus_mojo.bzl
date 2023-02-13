load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_codehaus_mojo_mrm_api",
        artifact = "org.codehaus.mojo:mrm-api:1.3.0",
        artifact_sha256 = "a16d141933e6ce6bc324b9937c89cebd146fed1579b2ec1c2ad5038df0af5cff",
        srcjar_sha256 = "1b2802f5c51dd3570165da38f08fef97c788ada3dcc905b617194b005dae7d9e",
        deps = [
            "@org_apache_maven_archetype_archetype_common",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_artifact_manager",
            "@org_apache_maven_maven_plugin_api",
            "@org_apache_maven_maven_repository_metadata",
        ],
    )


    import_external(
        name = "org_codehaus_mojo_mrm_maven_plugin",
        artifact = "org.codehaus.mojo:mrm-maven-plugin:1.3.0",
        artifact_sha256 = "c92f6e891a26564e87cd9ce4558aef185992d26ee61e1efcf7f642e65bb09a49",
        srcjar_sha256 = "bd951ef479ad04ee0c84494354f4f9dc2eb33103b853337e92a07c50d6fd0652",
        deps = [
            "@commons_lang_commons_lang",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_core",
            "@org_apache_maven_maven_model",
            "@org_apache_maven_maven_plugin_api",
            "@org_apache_maven_maven_project",
            "@org_apache_maven_maven_repository_metadata",
            "@org_apache_maven_shared_maven_common_artifact_filters",
            "@org_codehaus_mojo_mrm_api",
            "@org_codehaus_mojo_mrm_servlet",
            "@org_codehaus_plexus_plexus_utils",
            "@org_eclipse_jetty_jetty_server",
            "@org_eclipse_jetty_jetty_webapp",
        ],
        excludes = [
            "org.mortbay.jetty:servlet-api",
        ],
    )


    import_external(
        name = "org_codehaus_mojo_mrm_servlet",
        artifact = "org.codehaus.mojo:mrm-servlet:1.3.0",
        artifact_sha256 = "3882b3f29912bbacad98d966b23697139688bfd305123a4801e62cafafc21192",
        srcjar_sha256 = "dabbf949207b1e8c8db077e0fd95737581c1aff72574f5222c6d0d4e0b518093",
        deps = [
            "@commons_io_commons_io",
            "@commons_lang_commons_lang",
            "@org_apache_maven_archetype_archetype_common",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_model",
            "@org_apache_maven_maven_plugin_api",
            "@org_apache_maven_maven_repository_metadata",
            "@org_codehaus_mojo_mrm_api",
            "@org_codehaus_plexus_plexus_archiver",
            "@org_codehaus_plexus_plexus_utils",
        ],
        excludes = [
            "org.mortbay.jetty:servlet-api",
        ],
    )
