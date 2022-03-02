load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_javassist_javassist",
        artifact = "org.javassist:javassist:3.26.0-GA",
        artifact_sha256 = "ca5625874ff0a34f2422173a511b33c225218c146a3c961b18940efff430462d",
        srcjar_sha256 = "32f4ae4e803fb6964ba5c4104e0e997a0d05e07da99b454cf13a63b85e3697f1",
    )
