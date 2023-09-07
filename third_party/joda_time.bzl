load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "joda_time_joda_time",
        artifact = "joda-time:joda-time:2.10.10",
        artifact_sha256 = "dd8e7c92185a678d1b7b933f31209b6203c8ffa91e9880475a1be0346b9617e3",
        srcjar_sha256 = "c614905f496e72311255cf3790bcc7e1cd22db5db0d5e705448cc808966e81e3",
    )
