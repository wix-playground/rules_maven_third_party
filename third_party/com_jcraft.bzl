load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_jcraft_jsch",
        artifact = "com.jcraft:jsch:0.1.38",
        artifact_sha256 = "fa463ba278e26e4fe50cc4fac9a4d3f0b75f89d0a1bc6a013b66bf8b2c9a3651",
    )
