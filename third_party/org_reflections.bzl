load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_reflections_reflections",
        artifact = "org.reflections:reflections:0.9.12",
        artifact_sha256 = "d168f58d32f2ae7ac5a8d5d9092adeee526c604b41125dcb45eea877960a99cf",
        srcjar_sha256 = "2128daae26594a3fa006b93048cc0f9d19a3c6f42de4f96274fc04623a866c16",
        deps = [
            "@org_javassist_javassist",
        ],
    )
