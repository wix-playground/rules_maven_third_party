load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_maven_shared_maven_common_artifact_filters",
        artifact = "org.apache.maven.shared:maven-common-artifact-filters:3.2.0",
        artifact_sha256 = "5140bfa7f6cb38de987d084ac32bd07dedd353f5ad183da20395a0fca3c82498",
        srcjar_sha256 = "a9142a0eaf48fd57fa22472a0df71b70070af3743fbaee9ec5a7f20932392bec",
        deps = [
            "@commons_io_commons_io",
            "@org_apache_maven_maven_artifact",
            "@org_apache_maven_maven_core",
            "@org_apache_maven_maven_model",
            "@org_apache_maven_maven_plugin_api",
            "@org_apache_maven_shared_maven_shared_utils",
            "@org_eclipse_aether_aether_api",
            "@org_eclipse_aether_aether_util",
            "@org_eclipse_sisu_org_eclipse_sisu_plexus",
        ],
    )


    import_external(
        name = "org_apache_maven_shared_maven_invoker",
        artifact = "org.apache.maven.shared:maven-invoker:2.0.11",
        artifact_sha256 = "a577ad3ff71bc8714120b4083f6c4a71d71efa1893c76277e50439781118e28a",
        srcjar_sha256 = "0ff25aec6c2b3f41a49445e750a57d25bce9647acbfbcfe68655eb98a8d31106",
        deps = [
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_shared_maven_shared_utils",
        artifact = "org.apache.maven.shared:maven-shared-utils:3.3.3",
        artifact_sha256 = "44a60c610f4e31524b03d81a698b1ecceba116320ea510babf859575b2ea7233",
        srcjar_sha256 = "72ca9df1471a9042c379731cd204ce8a8039a11d6271f963ead65b90aba0d443",
        deps = [
            "@commons_io_commons_io",
        ],
    )
