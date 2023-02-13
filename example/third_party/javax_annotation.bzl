load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "javax_annotation_javax_annotation_api",
        artifact = "javax.annotation:javax.annotation-api:1.3.2",
        artifact_sha256 = "e04ba5195bcd555dc95650f7cc614d151e4bcd52d29a10b8aa2197f3ab89ab9b",
        srcjar_sha256 = "128971e52e0d84a66e3b6e049dab8ad7b2c58b7e1ad37fa2debd3d40c2947b95",
    )


    import_external(
        name = "javax_annotation_jsr250_api",
        artifact = "javax.annotation:jsr250-api:1.0",
        artifact_sha256 = "a1a922d0d9b6d183ed3800dfac01d1e1eb159f0e8c6f94736931c1def54a941f",
        srcjar_sha256 = "025c47d76c60199381be07012a0c5f9e74661aac5bd67f5aec847741c5b7f838",
    )
