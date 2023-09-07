load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_portable_scala_portable_scala_reflect_2_12",
        artifact = "org.portable-scala:portable-scala-reflect_2.12:1.1.1",
        artifact_sha256 = "2eb8ee9df8ff1ecb3612552d2f6dbdbc17c149ad78dbeef5ca56bc6ba964a956",
        srcjar_sha256 = "da225cd34412a66905e5d9f6bfa03c28933c41bb4ff44b468bdd573009c2758c",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )
