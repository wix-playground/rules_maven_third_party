load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_apache_jackrabbit_jackrabbit_jcr_commons",
        artifact = "org.apache.jackrabbit:jackrabbit-jcr-commons:1.5.0",
        artifact_sha256 = "34459359f8a5b22447272125fdd5df4bab4d4d831010cdea60c4f7acdcf7413b",
        srcjar_sha256 = "1615d73ae46618e606c13914adbfca29642acfbc245f3cdb76f573fe6b001172",
    )


    import_external(
        name = "org_apache_jackrabbit_jackrabbit_webdav",
        artifact = "org.apache.jackrabbit:jackrabbit-webdav:1.5.0",
        artifact_sha256 = "3a3110851517c8f62be7262716b08349b281e3fd15452c56b8a6dc4ca5696975",
        srcjar_sha256 = "b6a91602923a5d4fa7711b67bbc821c33904f47a1b6acba2b1b8108785c3d635",
        deps = [
            "@org_slf4j_slf4j_api",
        ],
        runtime_deps = [
            "@commons_httpclient_commons_httpclient",
            "@org_apache_jackrabbit_jackrabbit_jcr_commons",
        ],
        excludes = [
            "commons-logging:commons-logging",
        ],
    )
