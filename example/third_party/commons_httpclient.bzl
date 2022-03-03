load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "commons_httpclient_commons_httpclient",
        artifact = "commons-httpclient:commons-httpclient:3.0",
        artifact_sha256 = "7afd18f30e98c92bf873b64bafeea43b4abdeaba62a4e50e1b6b2d00405ef7ef",
        srcjar_sha256 = "9ae59d6a3ff7ad20cb36b946b1c7a80db5124825d701ff8ede4464c422ec251e",
        deps = [
            "@commons_codec_commons_codec",
        ],
        excludes = [
            "junit:junit",
        ],
    )
