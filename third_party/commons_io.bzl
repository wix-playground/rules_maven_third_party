load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "commons_io_commons_io",
        artifact = "commons-io:commons-io:2.0.1",
        artifact_sha256 = "2a3f5a206480863aae9dff03f53c930c3add6912f8785498d59442c7ebb98c5c",
        srcjar_sha256 = "9efca5493dca44111bd71c6a8b8d902ee9a097cbc346bc8b98c1c140c94ebfc9",
    )
