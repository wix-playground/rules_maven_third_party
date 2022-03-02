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
        artifact = "org.apache.commons:commons-lang3:3.5",
        artifact_sha256 = "8ac96fc686512d777fca85e144f196cd7cfe0c0aec23127229497d1a38ff651c",
        srcjar_sha256 = "1f7adeee4d483a6ca8d479d522cb2b07e39d976b758f3c2b6e1a0fed20dcbd2d",
    )
