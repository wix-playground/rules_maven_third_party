load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_google_j2objc_j2objc_annotations",
        artifact = "com.google.j2objc:j2objc-annotations:1.3",
        artifact_sha256 = "21af30c92267bd6122c0e0b4d20cccb6641a37eaf956c6540ec471d584e64a7b",
        srcjar_sha256 = "ba4df669fec153fa4cd0ef8d02c6d3ef0702b7ac4cabe080facf3b6e490bb972",
    )
