load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_apache_maven_resolver_maven_resolver_api",
        artifact = "org.apache.maven.resolver:maven-resolver-api:1.7.3",
        artifact_sha256 = "068bdab0cb4534cf8fa5e8b26e23898f26393e0e20c97c3a1a6b59ce86a9acdb",
        srcjar_sha256 = "f5c85779ec2a70e3aaae971b4784de6434e38dbb82d05519e6dcb2461f1a59b8",
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_connector_basic",
        artifact = "org.apache.maven.resolver:maven-resolver-connector-basic:1.7.3",
        artifact_sha256 = "29beb02541adb82873d7bdeed2aaea03eaf3123720dbd1b5d96d058849364c66",
        srcjar_sha256 = "8f97266525f37035a3bbe0c9d156f747ffc89672591300ce1a379d124e594f3d",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_apache_maven_resolver_maven_resolver_util",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_impl",
        artifact = "org.apache.maven.resolver:maven-resolver-impl:1.7.3",
        artifact_sha256 = "d49ad3a980a8a5ea3761fb312a784673806eecdeb380c1a8b97d55786c8ac625",
        srcjar_sha256 = "eb0e30e55088c064c2cad736b3d1c46569037bec3f4df545714d60d8b04196dd",
        deps = [
            "@javax_annotation_javax_annotation_api",
            "@org_apache_commons_commons_lang3",
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_named_locks",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_apache_maven_resolver_maven_resolver_util",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_named_locks",
        artifact = "org.apache.maven.resolver:maven-resolver-named-locks:1.7.3",
        artifact_sha256 = "51fa0f7f410a3a8690ddb28e512797ce78a750b0eb98234c05f76bbac91c6b79",
        srcjar_sha256 = "fa545bf7234b38ed8a385074c75d5e65db5deaffa9847fd24c6909d52518a2e4",
        deps = [
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_spi",
        artifact = "org.apache.maven.resolver:maven-resolver-spi:1.7.3",
        artifact_sha256 = "26df4115fb0a33770ca00166a197ba95d378296a5ac86d2a4253747498f7a1a9",
        srcjar_sha256 = "ca925643b03603c5634941d4bc32e65d3c77a8df0aec03d4ce420678006afa24",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_transport_file",
        artifact = "org.apache.maven.resolver:maven-resolver-transport-file:1.7.3",
        artifact_sha256 = "13af66cf931e62a141b3508e9d6b21579be84fdb3f627015d4fce6fcfd53b29d",
        srcjar_sha256 = "61599f0db2fde31d19d6da4d0a5d503b8fbb35b049958cbc29cc50a35865659f",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
            "@org_apache_maven_resolver_maven_resolver_spi",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_apache_maven_resolver_maven_resolver_transport_http",
        artifact = "org.apache.maven.resolver:maven-resolver-transport-http:1.7.3",
        artifact_sha256 = "558cbdf4d76f7cf3699b59fb22fd65614870e19b42983585b42ce84f336b348f",
        srcjar_sha256 = "e6365be5690c4146a5d809c1036a599eadd9c00ae82b1d4b878b36178deb7d2e",
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
        artifact = "org.apache.maven.resolver:maven-resolver-util:1.7.3",
        artifact_sha256 = "62839a6a3df08b6d5a98edce8fc26840a3f6e849e58618cd0917c0d4a3381e04",
        srcjar_sha256 = "0c9f4ada2710b9e18f58abddceb729e4bce03be745bc6f5c0de932e4d7e6bb08",
        deps = [
            "@org_apache_maven_resolver_maven_resolver_api",
        ],
    )
