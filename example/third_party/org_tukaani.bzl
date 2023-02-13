load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_tukaani_xz",
        artifact = "org.tukaani:xz:1.9",
        artifact_sha256 = "211b306cfc44f8f96df3a0a3ddaf75ba8c5289eed77d60d72f889bb855f535e5",
        srcjar_sha256 = "5befa47f06b90e752f035191dde7f2deb59f36000f1ca6cc77d2362a82b6f462",
    )
