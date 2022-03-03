load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_commons_commons_compress",
        artifact = "org.apache.commons:commons-compress:1.11",
        artifact_sha256 = "9fc905a68dcf3038d4866a54040706998e0202a34ecd95d734e819b0bccf439e",
        srcjar_sha256 = "411d156677d380fcba28da0e7b7547e96b8a99c5f162fdf80af0b8d342514a94",
    )


    import_external(
        name = "org_apache_commons_commons_lang3",
        artifact = "org.apache.commons:commons-lang3:3.8.1",
        artifact_sha256 = "dac807f65b07698ff39b1b07bfef3d87ae3fd46d91bbf8a2bc02b2a831616f68",
        srcjar_sha256 = "a6589a5acef187a9c032b2afe22384acc3ae0bf15bb91ff67db8731ebb4323ca",
    )
