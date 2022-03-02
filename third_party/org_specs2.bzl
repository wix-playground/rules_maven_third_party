load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_specs2_classycle",
        artifact = "org.specs2:classycle:1.4.3",
        artifact_sha256 = "9b8cc4f88a5fa8c0e9437ff72f472f9f8e2a7509d94261df6196d5570935d697",
        srcjar_sha256 = "23e4f4afe7f91b882974fa7fa06164c2db1b1d97a1d974cde166ed0990d03da1",
    )


    import_external(
        name = "org_specs2_specs2_analysis_2_12",
        artifact = "org.specs2:specs2-analysis_2.12:4.8.3",
        artifact_sha256 = "2286ff5a1ab6f10e5ea3f8a013620126fc5cc4ed2105646b6e352d65abcdac7e",
        srcjar_sha256 = "31866f1edc3538f7feb56595d8aff156f9c2dd113a9b3d991f1a2ebf7f39ddac",
        deps = [
            "@org_scala_lang_scala_compiler",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_test_interface",
            "@org_specs2_classycle",
            "@org_specs2_specs2_core_2_12",
            "@org_specs2_specs2_matcher_2_12",
        ],
    )


    import_external(
        name = "org_specs2_specs2_common_2_12",
        artifact = "org.specs2:specs2-common_2.12:4.8.3",
        artifact_sha256 = "3b08fecb9e21d3903e48b62cd95c19ea9253d466e03fd4cf9dc9227e7c368708",
        srcjar_sha256 = "b2f148c75d3939b3cd0d58afddd74a8ce03077bb3ccdc93dae55bd9c3993e9c3",
        deps = [
            "@org_scala_lang_modules_scala_parser_combinators_2_12",
            "@org_scala_lang_modules_scala_xml_2_12",
            "@org_scala_lang_scala_library",
            "@org_scala_lang_scala_reflect",
            "@org_specs2_specs2_fp_2_12",
        ],
    )


    import_external(
        name = "org_specs2_specs2_core_2_12",
        artifact = "org.specs2:specs2-core_2.12:4.8.3",
        artifact_sha256 = "f73f32156a711a4e83e696dc83e269c5a165d62cc3dd7c652617cb03d140d063",
        srcjar_sha256 = "0e3cebfc7410051b70e627e35f13978add3d061b8f1233741f9b397638f193e9",
        deps = [
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_test_interface",
            "@org_specs2_specs2_common_2_12",
            "@org_specs2_specs2_matcher_2_12",
        ],
    )


    import_external(
        name = "org_specs2_specs2_fp_2_12",
        artifact = "org.specs2:specs2-fp_2.12:4.8.3",
        artifact_sha256 = "777962ca58054a9ea86e294e025453ecf394c60084c28bd61956a00d16be31a7",
        srcjar_sha256 = "6b8bd1e7210754b768b68610709271c0dac29447936a976a2a9881389e6404ca",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "org_specs2_specs2_junit_2_12",
        artifact = "org.specs2:specs2-junit_2.12:4.8.3",
        artifact_sha256 = "5d7ad2c0b0bc142ea064edb7a1ea75ab7b17ad37e1a621ac7e578823845098e8",
        srcjar_sha256 = "84edd1cd6291f6686638225fcbaff970ae36da006efabf2228255c2127b2290c",
        deps = [
            "@junit_junit",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_test_interface",
            "@org_specs2_specs2_core_2_12",
        ],
    )


    import_external(
        name = "org_specs2_specs2_matcher_2_12",
        artifact = "org.specs2:specs2-matcher_2.12:4.8.3",
        artifact_sha256 = "aadf27b6d015572b2e3842627c09bf0797153dbb329262ea3bcbbce129d51ad8",
        srcjar_sha256 = "01251acc28219aa17aabcb9a26a84e1871aa64980d335cd8f83c2bcea6f4f1be",
        deps = [
            "@org_scala_lang_scala_library",
            "@org_specs2_specs2_common_2_12",
        ],
    )


    import_external(
        name = "org_specs2_specs2_matcher_extra_2_12",
        artifact = "org.specs2:specs2-matcher-extra_2.12:4.8.3",
        artifact_sha256 = "8fdf761bf296f7fb880eb0ee1cddbcfe06a8a07a27d1a06be10512b56d433774",
        srcjar_sha256 = "acf622bd51e4363688f946751ef3900d102227a1bffe283df419fa75ca67a136",
        deps = [
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_test_interface",
            "@org_specs2_specs2_analysis_2_12",
            "@org_specs2_specs2_matcher_2_12",
        ],
    )


    import_external(
        name = "org_specs2_specs2_mock_2_12",
        artifact = "org.specs2:specs2-mock_2.12:4.8.3",
        artifact_sha256 = "4e4cf80dd3ef138758bafe6c05721fe5f16ca13412a344797a709129d79b910b",
        srcjar_sha256 = "2314dda8807d272038814fdaae4247e45fb7d209361eb5cbf444a2adaef4760d",
        deps = [
            "@org_hamcrest_hamcrest_core",
            "@org_mockito_mockito_core",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_test_interface",
            "@org_specs2_specs2_core_2_12",
        ],
    )


    import_external(
        name = "org_specs2_specs2_shapeless_2_12",
        artifact = "org.specs2:specs2-shapeless_2.12:4.11.0",
        artifact_sha256 = "ed07cc0c85a12570ca6d22f9dcab81aa6a48187095d29db1e7c30f77f9e1726c",
        srcjar_sha256 = "5edc561a0adbcef47a1bae367bb99d15fdc0c880d862af614e9bd38079e6abdb",
        deps = [
            "@com_chuusai_shapeless_2_12",
            "@org_portable_scala_portable_scala_reflect_2_12",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_test_interface",
            "@org_specs2_specs2_matcher_2_12",
        ],
    )
