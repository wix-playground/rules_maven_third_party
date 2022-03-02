load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_mockito_mockito_core",
        artifact = "org.mockito:mockito-core:3.2.4",
        artifact_sha256 = "587dcc78914585a7ba7933417237dfcf4d4a1a295d1aa8091d732a76a1d8fe92",
        srcjar_sha256 = "7a18f4810a25ad1b43deceb9de1f1986384017f74ad251ab08501c3d8e4e40b0",
        deps = [
            "@net_bytebuddy_byte_buddy",
            "@net_bytebuddy_byte_buddy_agent",
            "@org_objenesis_objenesis",
        ],
    )
