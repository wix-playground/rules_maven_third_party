load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "com_typesafe_config",
        artifact = "com.typesafe:config:1.4.0",
        artifact_sha256 = "aadbfd5a524551beef10d3f891d305b83bb27d54703d9a4de7aca2a12d9847e2",
        srcjar_sha256 = "ffaf8892dc8c61605bd7319c6cdcea022b6c9c28b62776915a809e8de93d8a6e",
    )


    import_external(
        name = "com_typesafe_ssl_config_core_2_12",
        artifact = "com.typesafe:ssl-config-core_2.12:0.4.2",
        artifact_sha256 = "c9f16f354a2285d47f73188a0abee6c1e8be3f302b634a22c0a61810c10f4aaa",
        srcjar_sha256 = "c7674e8810f55f4677de9a30fd8c48c75533183a0adc453a14e72f43daef3766",
        deps = [
            "@com_typesafe_config",
            "@org_scala_lang_modules_scala_parser_combinators_2_12",
            "@org_scala_lang_scala_library",
        ],
    )
