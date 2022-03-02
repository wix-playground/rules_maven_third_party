load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_typesafe_akka_akka_actor_2_12",
        artifact = "com.typesafe.akka:akka-actor_2.12:2.6.16",
        artifact_sha256 = "a46d5d4e477228995a2326e06fb73fe48ef9bec039fe4ba6bb62fa5fc3f7dcad",
        srcjar_sha256 = "ad8d6745f3b7f2c527696aa767064e38549becd5775ff62568ca40af878db5e7",
        deps = [
            "@com_typesafe_config",
            "@org_scala_lang_modules_scala_java8_compat_2_12",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "com_typesafe_akka_akka_http_2_12",
        artifact = "com.typesafe.akka:akka-http_2.12:10.2.6",
        artifact_sha256 = "9af5c1fae52745b071b4b288629d32b2868f078e3845f450a3165053c44eeb51",
        srcjar_sha256 = "756cf337cb03d5ca4e471b7583549a1ddf262bd9c04caa8b15166e97386cb04e",
        deps = [
            "@com_typesafe_akka_akka_http_core_2_12",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "com_typesafe_akka_akka_http_core_2_12",
        artifact = "com.typesafe.akka:akka-http-core_2.12:10.2.6",
        artifact_sha256 = "a820cfbb024a37a96c60ff53556da2ad1919d60d206fd7882397a102d9bab264",
        srcjar_sha256 = "f843e9818a773c36e7a87963955d2104b1b541b93c443e68737239958cc34eb5",
        deps = [
            "@com_typesafe_akka_akka_parsing_2_12",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "com_typesafe_akka_akka_parsing_2_12",
        artifact = "com.typesafe.akka:akka-parsing_2.12:10.2.6",
        artifact_sha256 = "47f51220de30cf7483106f28b83f6f2bd0f9cb595306c121048c174b24594789",
        srcjar_sha256 = "5f7745755af57b1a535db598d2bad49b85bdcfd4b61851e7bc9663dd738e1c6f",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "com_typesafe_akka_akka_protobuf_v3_2_12",
        artifact = "com.typesafe.akka:akka-protobuf-v3_2.12:2.6.16",
        artifact_sha256 = "9d3d8fbd88683164fa546f262137b204dedeeca7094e729ed3d396704e101119",
        srcjar_sha256 = "2f2e15b9dcb08d8dc83c4ca564b5b8d05ce771738fa7d45f81440c2519988c42",
    )


    import_external(
        name = "com_typesafe_akka_akka_stream_2_12",
        artifact = "com.typesafe.akka:akka-stream_2.12:2.6.16",
        artifact_sha256 = "9cf929254e7b2fc335a931463f03272952ab1b9eb8544b8743c992554d835a7b",
        srcjar_sha256 = "e659012d340a3d5c38744ea2e25a2b33f0457093224458eab5bcbffea28cdcad",
        deps = [
            "@com_typesafe_akka_akka_actor_2_12",
            "@com_typesafe_akka_akka_protobuf_v3_2_12",
            "@com_typesafe_ssl_config_core_2_12",
            "@org_reactivestreams_reactive_streams",
            "@org_scala_lang_scala_library",
        ],
    )
