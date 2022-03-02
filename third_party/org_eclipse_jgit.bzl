load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_eclipse_jgit_org_eclipse_jgit",
        artifact = "org.eclipse.jgit:org.eclipse.jgit:5.11.0.202103091610-r",
        artifact_sha256 = "b0f012105d67729a67c7fde546b6e89580f7ddc5bd73c6c7bae7084c50e36a37",
        srcjar_sha256 = "23b4f2debe38b2e18cb925ada6639eb78cc029243060f8f8c080ba3e0e70ab71",
        deps = [
            "@com_googlecode_javaewah_JavaEWAH",
            "@org_slf4j_slf4j_api",
        ],
    )
