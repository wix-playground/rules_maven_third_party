load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "com_google_guava_failureaccess",
        artifact = "com.google.guava:failureaccess:1.0.1",
        artifact_sha256 = "a171ee4c734dd2da837e4b16be9df4661afab72a41adaf31eb84dfdaf936ca26",
        srcjar_sha256 = "092346eebbb1657b51aa7485a246bf602bb464cc0b0e2e1c7e7201fadce1e98f",
    )


    import_external(
        name = "com_google_guava_guava",
        artifact = "com.google.guava:guava:31.0.1-jre",
        artifact_sha256 = "d5be94d65e87bd219fb3193ad1517baa55a3b88fc91d21cf735826ab5af087b9",
        srcjar_sha256 = "fc0fb66f315f10b8713fc43354936d3649a8ad63f789d42fd7c3e55ecf72e092",
        deps = [
            "@com_google_code_findbugs_jsr305",
            "@com_google_errorprone_error_prone_annotations",
            "@com_google_guava_failureaccess",
            "@com_google_guava_listenablefuture",
            "@com_google_j2objc_j2objc_annotations",
            "@org_checkerframework_checker_qual",
        ],
    )


    import_external(
        name = "com_google_guava_listenablefuture",
        artifact = "com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava",
        artifact_sha256 = "b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99",
    )
