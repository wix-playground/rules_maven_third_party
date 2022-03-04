load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_google_errorprone_error_prone_annotations",
        artifact = "com.google.errorprone:error_prone_annotations:2.7.1",
        artifact_sha256 = "cd5257c08a246cf8628817ae71cb822be192ef91f6881ca4a3fcff4f1de1cff3",
        srcjar_sha256 = "e38921f918b8ad8eabd12bc61de426fa96c72de077054e9147d2f9fe7c648923",
    )
