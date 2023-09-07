load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "commons_io_commons_io",
        artifact = "commons-io:commons-io:2.11.0",
        artifact_sha256 = "961b2f6d87dbacc5d54abf45ab7a6e2495f89b75598962d8c723cea9bc210908",
        srcjar_sha256 = "8c5746d4e96ed0300a7252b1d4cb65111b19400d28a929ca8e0e4b637875f1ee",
    )
