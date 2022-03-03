load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_scalaj_scalaj_http_2_12",
        artifact = "org.scalaj:scalaj-http_2.12:2.4.2",
        artifact_sha256 = "2be03f9b1680dbb34cfca3021c2f94a7c0441d70b028321639abdb67a45b0a8b",
        srcjar_sha256 = "630dd6813bbeef7cf80ca7c0fe10671b4faafcbbbe1f23d25b4e4b3ef5504792",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )
