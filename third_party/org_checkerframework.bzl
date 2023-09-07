load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_checkerframework_checker_qual",
        artifact = "org.checkerframework:checker-qual:3.12.0",
        artifact_sha256 = "ff10785ac2a357ec5de9c293cb982a2cbb605c0309ea4cc1cb9b9bc6dbe7f3cb",
        srcjar_sha256 = "fd99a45195ed893803624d1030387056a96601013f5e61ccabd79abb4ddfa876",
    )
