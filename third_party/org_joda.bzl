load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_joda_joda_convert",
        artifact = "org.joda:joda-convert:2.2.1",
        artifact_sha256 = "994db5e743c4598e6798f495cc320cfeda4393d06bd9dfb1b8a44ae0b5e10b85",
        srcjar_sha256 = "abd0b6b201b5b4450274937a6be176622ac13c8dd22530fb6fc9cfd5db567114",
    )
