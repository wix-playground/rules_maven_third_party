load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_fusesource_jansi_jansi",
        artifact = "org.fusesource.jansi:jansi:1.18",
        artifact_sha256 = "109e64fc65767c7a1a3bd654709d76f107b0a3b39db32cbf11139e13a6f5229b",
        srcjar_sha256 = "dcddb789ebf8efd8cedbafad6619a510fb5489600edd53d6c3aed5365fba215d",
    )
