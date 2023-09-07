load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_apache_httpcomponents_httpclient",
        artifact = "org.apache.httpcomponents:httpclient:4.5.13",
        artifact_sha256 = "6fe9026a566c6a5001608cf3fc32196641f6c1e5e1986d1037ccdbd5f31ef743",
        srcjar_sha256 = "b1e9194fd83ce135831e28346731d9644cb2a08dea37ada2aa56ceb8f1b0c566",
        deps = [
            "@commons_codec_commons_codec",
            "@org_apache_httpcomponents_httpcore",
        ],
        excludes = [
            "commons-logging:commons-logging",
        ],
    )


    import_external(
        name = "org_apache_httpcomponents_httpcore",
        artifact = "org.apache.httpcomponents:httpcore:4.4.14",
        artifact_sha256 = "f956209e450cb1d0c51776dfbd23e53e9dd8db9a1298ed62b70bf0944ba63b28",
        srcjar_sha256 = "7a7f721f3bd1b09de508bf6811931dd9527e4fa3d643d5ccc7cef3fa2d4ee3df",
    )
