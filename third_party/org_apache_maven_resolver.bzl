load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_maven_resolver_maven_resolver_api",
        artifact = "org.apache.maven.resolver:maven-resolver-api:1.4.0",
        artifact_sha256 = "85aac254240e8bf387d737acf5fcd18f07163ae55a0223b107c7e2af1dfdc6e6",
        srcjar_sha256 = "be7f42679a5485fbe30c475afa05c12dd9a2beb83bbcebbb3d2e79eb8aeff9c4",
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_connector_basic",
        artifact = "org.apache.maven.resolver:maven-resolver-connector-basic:1.4.0",
        artifact_sha256 = "4283db771d9265136615637bd22d02929cfd548c8d351f76ecb88a3006b5faf7",
        srcjar_sha256 = "556163b53b1f98df263adf1d26b269cd45316a827f169e0ede514ca5fca0c5d1",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_apache_maven_resolver_maven_resolver_util",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_impl",
        artifact = "org.apache.maven.resolver:maven-resolver-impl:1.4.0",
        artifact_sha256 = "004662079feeed66251480ad76fedbcabff96ee53db29c59f6aa564647c5bfe6",
        srcjar_sha256 = "b544f134261f813b1a44ffcc97590236d3d6e2519722d55dea395a96fef18206",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_apache_maven_resolver_maven_resolver_util",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_spi",
        artifact = "org.apache.maven.resolver:maven-resolver-spi:1.4.0",
        artifact_sha256 = "8a2985eb28135eae4c40db446081b1533c1813c251bb370756777697e0b7114e",
        srcjar_sha256 = "89099a02006b6ce46096d89f021675bf000e96300bcdc0ff439a86d6e322c761",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_transport_file",
        artifact = "org.apache.maven.resolver:maven-resolver-transport-file:1.4.0",
        artifact_sha256 = "94eb9bcc073ac1591002b26a4cf558324b12d8f76b6d5628151d7f87733436f6",
        srcjar_sha256 = "17abd750063fa74cbf754e803ba27ca0216b0bebc8e45e1872cd9ed5a1e5e719",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_transport_http",
        artifact = "org.apache.maven.resolver:maven-resolver-transport-http:1.4.0",
        artifact_sha256 = "8dddd83ec6244bde5ef63ae679a0ce5d7e8fc566369d7391c8814206e2a7114f",
        srcjar_sha256 = "5af0150a1ab714b164763d1daca4b8fdd1ab6dd445ec3c57e7ec916ccbdf7e4e",
        deps = [
            "@org_apache_httpcomponents_httpclient",
            "@org_apache_httpcomponents_httpcore",
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_apache_maven_resolver_maven_resolver_util",
            "@org_slf4j_jcl_over_slf4j",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_util",
        artifact = "org.apache.maven.resolver:maven-resolver-util:1.4.0",
        artifact_sha256 = "e83b6c2de4b8b8d99d3c226f5e447f70df808834824336c360aa615fc4d7beac",
        srcjar_sha256 = "74dd3696e2df175db39b944079f7b49941e39e57f98e469f942635a2ba1cae57",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
        ],
    )
