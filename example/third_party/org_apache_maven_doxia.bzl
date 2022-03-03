load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_maven_doxia_doxia_logging_api",
        artifact = "org.apache.maven.doxia:doxia-logging-api:1.1",
        artifact_sha256 = "80f1b67a2f698f0e8dd11e5cedfc28c5b8e6fb2986adf939bfa04d92d9367d66",
        srcjar_sha256 = "ec68bd47c99ee3662ce6f79d4ebaddcab3ebff79781171801f164022d164afbd",
        deps = [
            "@org_codehaus_plexus_plexus_container_default",
        ],
    )


    import_external(
        name = "org_apache_maven_doxia_doxia_sink_api",
        artifact = "org.apache.maven.doxia:doxia-sink-api:1.1",
        artifact_sha256 = "c59e706156064a6a02444212b16cec3f3403bd626f124223abeaaf8f66447e92",
        srcjar_sha256 = "ebf8ea7b0f0f2f52759ffb24789bfb39c265adc7dfc0e648f9bb2477259529bb",
        deps = [
            "@org_apache_maven_doxia_doxia_logging_api",
        ],
    )
