load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "commons_fileupload_commons_fileupload",
        artifact = "commons-fileupload:commons-fileupload:1.4",
        artifact_sha256 = "a4ec02336f49253ea50405698b79232b8c5cbf02cb60df3a674d77a749a1def7",
        srcjar_sha256 = "2acfe29671daf8c94be5d684b8ac260d9c11f78611dff4899779b43a99205291",
        excludes = [
            "commons-io:commons-io",
        ],
    )
