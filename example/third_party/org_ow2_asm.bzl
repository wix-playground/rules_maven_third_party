load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_ow2_asm_asm",
        artifact = "org.ow2.asm:asm:9.2",
        artifact_sha256 = "b9d4fe4d71938df38839f0eca42aaaa64cf8b313d678da036f0cb3ca199b47f5",
        srcjar_sha256 = "81e807010631f0e8074b0fb85e80afd6efbbd7e4b3694aad19e944c171980fb7",
    )
