load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_fasterxml_jackson_core_jackson_annotations",
        artifact = "com.fasterxml.jackson.core:jackson-annotations:2.9.9",
        artifact_sha256 = "1100a5884ddc4439a77165e1b9668c6063c07447cd2f6c9f69e3688ee76080c1",
        srcjar_sha256 = "2aab9fdd4e8ca075e8842bf8e1a787c48e905c6e2afb3d40fba5e22336f5ef85",
    )


    import_external(
        name = "com_fasterxml_jackson_core_jackson_core",
        artifact = "com.fasterxml.jackson.core:jackson-core:2.9.9",
        artifact_sha256 = "3083079be6088db2ed0a0c6ff92204e0aa48fa1de9db5b59c468f35acf882c2c",
        srcjar_sha256 = "cfc968701a863296c8d44b36526588c850ac0dfd8dff7610b024336068caac0e",
    )


    import_external(
        name = "com_fasterxml_jackson_core_jackson_databind",
        artifact = "com.fasterxml.jackson.core:jackson-databind:2.9.9",
        artifact_sha256 = "5cbbf429d9e32e3881f0a1438a1f666912219327e9e68b5dcaef6d8e5c5f6b28",
        srcjar_sha256 = "d78ee97c1041f83c3a1706e0f01abfa91c890fcf2c622c1ef602187c76768985",
        deps = [
            "@com_fasterxml_jackson_core_jackson_annotations",
            "@com_fasterxml_jackson_core_jackson_core",
        ],
    )
