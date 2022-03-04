load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_commons_commons_compress",
        artifact = "org.apache.commons:commons-compress:1.21",
        artifact_sha256 = "6aecfd5459728a595601cfa07258d131972ffc39b492eb48bdd596577a2f244a",
        srcjar_sha256 = "f64973e7c00455e819bcf8b74dda6e91b3e09557135b11b3603c2ba7a0e5479b",
    )


    import_external(
        name = "org_apache_commons_commons_lang3",
        artifact = "org.apache.commons:commons-lang3:3.8.1",
        artifact_sha256 = "dac807f65b07698ff39b1b07bfef3d87ae3fd46d91bbf8a2bc02b2a831616f68",
        srcjar_sha256 = "a6589a5acef187a9c032b2afe22384acc3ae0bf15bb91ff67db8731ebb4323ca",
    )
