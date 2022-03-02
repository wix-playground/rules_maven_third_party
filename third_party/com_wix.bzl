load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_wix_http_testkit_2_12",
        artifact = "com.wix:http-testkit_2.12:0.1.25",
        artifact_sha256 = "b0d9ee9547422f0d0e6638129a9b061f99811a78abbe247e9918f3f069b16ccd",
        srcjar_sha256 = "713b67848fe96454249421219bd6c3fdafde556cf37d14b7f9cad86bdd27f2cd",
        deps = [
            "@com_wix_http_testkit_client_2_12",
            "@com_wix_http_testkit_server_2_12",
            "@com_wix_http_testkit_specs2_2_12",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "com_wix_http_testkit_client_2_12",
        artifact = "com.wix:http-testkit-client_2.12:0.1.25",
        artifact_sha256 = "e5ff58e2d6a98bde26385e0936fc9e77fde292d5f64005e820177711bf23dc58",
        srcjar_sha256 = "39dbf613f89192767cf73640d73e4697c5fafeaae4b30215bee861d9a7784aa0",
        deps = [
            "@com_wix_http_testkit_core_2_12",
            "@com_wix_http_testkit_specs2_2_12",
            "@org_scala_lang_scala_library",
            "@org_specs2_specs2_core_2_12",
            "@org_specs2_specs2_junit_2_12",
            "@org_specs2_specs2_mock_2_12",
            "@org_specs2_specs2_shapeless_2_12",
        ],
    )


    import_external(
        name = "com_wix_http_testkit_core_2_12",
        artifact = "com.wix:http-testkit-core_2.12:0.1.25",
        artifact_sha256 = "26a592dbd8afb6f55ed5778a15fbd56e504d86d4ac659920d725503f17791d8a",
        srcjar_sha256 = "e4f6d0d771903131eb085fdea78a238a9d66d46a4745e3cb44f0fdfd430d066f",
        deps = [
            "@com_google_code_findbugs_jsr305",
            "@com_typesafe_akka_akka_actor_2_12",
            "@com_typesafe_akka_akka_http_2_12",
            "@com_typesafe_akka_akka_stream_2_12",
            "@joda_time_joda_time",
            "@org_joda_joda_convert",
            "@org_reflections_reflections",
            "@org_scala_lang_modules_scala_xml_2_12",
            "@org_scala_lang_scala_library",
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "com_wix_http_testkit_marshaller_jackson_2_12",
        artifact = "com.wix:http-testkit-marshaller-jackson_2.12:0.1.25",
        artifact_sha256 = "a5e32022ab01d4afa8b67d68c3f7da309a4c18c69ec5712f358773728d5edbc6",
        srcjar_sha256 = "b5e8baff8f4dcbd0ae4223f7a954c1af76bd47e1097388e1d9a72567e4180188",
        deps = [
            "@com_fasterxml_jackson_core_jackson_databind",
            "@com_fasterxml_jackson_datatype_jackson_datatype_jdk8",
            "@com_fasterxml_jackson_datatype_jackson_datatype_joda",
            "@com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
            "@com_fasterxml_jackson_module_jackson_module_parameter_names",
            "@com_fasterxml_jackson_module_jackson_module_scala_2_12",
            "@com_wix_http_testkit_core_2_12",
            "@org_scala_lang_scala_library",
            "@org_specs2_specs2_core_2_12",
            "@org_specs2_specs2_junit_2_12",
            "@org_specs2_specs2_mock_2_12",
            "@org_specs2_specs2_shapeless_2_12",
        ],
    )


    import_external(
        name = "com_wix_http_testkit_server_2_12",
        artifact = "com.wix:http-testkit-server_2.12:0.1.25",
        artifact_sha256 = "da9d7a79b3042cdb8ea2a8b2c87539c8a39437432296861113b52ef7da1b3eab",
        srcjar_sha256 = "6485ac389eb87ce90fe3f314a8e111d6fb9d5854b036dfe7393d00998d3d4892",
        deps = [
            "@com_wix_http_testkit_core_2_12",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "com_wix_http_testkit_specs2_2_12",
        artifact = "com.wix:http-testkit-specs2_2.12:0.1.25",
        artifact_sha256 = "9681c9a02a8be5d41c9aecf4c3c89e36c142a66977a32e597b93d3a6a7a20747",
        srcjar_sha256 = "66d5e99b61b02ded1f4921c009744842ca41091ddcac7065e94479f9d4b9fffe",
        deps = [
            "@com_wix_http_testkit_core_2_12",
            "@org_scala_lang_scala_library",
            "@org_specs2_specs2_core_2_12",
            "@org_specs2_specs2_junit_2_12",
            "@org_specs2_specs2_mock_2_12",
            "@org_specs2_specs2_shapeless_2_12",
        ],
    )
