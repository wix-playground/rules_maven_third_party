load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "io_github_alexarchambault_concurrent_reference_hash_map",
        artifact = "io.github.alexarchambault:concurrent-reference-hash-map:1.0.0",
        artifact_sha256 = "6f23a489c24743f1109125554e0d1c6420ab784f36acedd80a0704c8873b9642",
        srcjar_sha256 = "78d9552cb737e3d2d5bff0e059d84a42284c748f20d75c4f70280abeb8241f1c",
    )
