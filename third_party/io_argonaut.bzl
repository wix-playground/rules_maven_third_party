load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "io_argonaut_argonaut_2_12",
        artifact = "io.argonaut:argonaut_2.12:6.2.5",
        artifact_sha256 = "a89474477cb3abd6473e48c4e7af3722743993a2c203272499c6e2cc79c012a3",
        srcjar_sha256 = "8328a4dbf49c1f2b96589995a05e89cac6a16d7fffbdccf794611c898528fbea",
        deps = [
            "@org_scala_lang_scala_reflect",
        ],
    )
