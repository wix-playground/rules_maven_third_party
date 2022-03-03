load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_httpcomponents_core5_httpcore5",
        artifact = "org.apache.httpcomponents.core5:httpcore5:5.1.1",
        artifact_sha256 = "948776c3055557114adba5df9f3b57b557ff0980d9a0104f247d70ac59d58215",
        srcjar_sha256 = "45c9f690d51c7b64786a7068afbcdbdfcc61baa38e306dafae9da6080c756205",
    )


    import_external(
        name = "org_apache_httpcomponents_core5_httpcore5_h2",
        artifact = "org.apache.httpcomponents.core5:httpcore5-h2:5.1.1",
        artifact_sha256 = "e9846b0574cfa77ddcb6e05f5edd4e385c1fbaaa5971456ee89afe129e3ff02e",
        srcjar_sha256 = "1e0b2fd6a9d6e42aa78d54d20be806b1df3ada290765085e1d0fcff31035e811",
        deps = [
            "@org_apache_httpcomponents_core5_httpcore5",
        ],
    )
