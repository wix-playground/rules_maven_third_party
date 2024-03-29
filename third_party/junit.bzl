load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "junit_junit",
        artifact = "junit:junit:4.13.2",
        artifact_sha256 = "8e495b634469d64fb8acfa3495a065cbacc8a0fff55ce1e31007be4c16dc57d3",
        srcjar_sha256 = "34181df6482d40ea4c046b063cb53c7ffae94bdf1b1d62695bdf3adf9dea7e3a",
        deps = [
            "@org_hamcrest_hamcrest_core",
        ],
    )
