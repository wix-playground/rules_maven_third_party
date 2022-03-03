load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_httpcomponents_client5_httpclient5",
        artifact = "org.apache.httpcomponents.client5:httpclient5:5.1",
        artifact_sha256 = "b7a30296763a4d5dbf840f0b79df7439cf3d2341c8990aee4111591b61b50935",
        srcjar_sha256 = "d7351329b7720b482766aca78d1c040225a1b4bbd723034c6f686bec538edddd",
        deps = [
            "@commons_codec_commons_codec",
            "@org_apache_httpcomponents_core5_httpcore5",
            "@org_apache_httpcomponents_core5_httpcore5_h2",
            "@org_slf4j_slf4j_api",
        ],
    )
