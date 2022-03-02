load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_objenesis_objenesis",
        artifact = "org.objenesis:objenesis:2.6",
        artifact_sha256 = "5e168368fbc250af3c79aa5fef0c3467a2d64e5a7bd74005f25d8399aeb0708d",
        srcjar_sha256 = "52d9f4dba531677fc074eff00ea07f22a1d42e5a97cc9e8571c4cd3d459b6be0",
    )
