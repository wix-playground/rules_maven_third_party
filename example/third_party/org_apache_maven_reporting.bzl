load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_maven_reporting_maven_reporting_api",
        artifact = "org.apache.maven.reporting:maven-reporting-api:2.2.1",
        artifact_sha256 = "7339e0e8cf04574e9ce484713385888ca6ac6adc578a60a8e311261537df8c77",
        srcjar_sha256 = "b6c292e84381760c191b3ee035eb7a6b23d3954454cec3feb1ce7dc373c2b9f4",
        deps = [
            "@org_apache_maven_doxia_doxia_logging_api",
            "@org_apache_maven_doxia_doxia_sink_api",
        ],
    )
