load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "io_get_coursier_coursier_2_12",
        artifact = "io.get-coursier:coursier_2.12:2.0.13",
        artifact_sha256 = "302ac8e3563c9647f22ba21da47656e5b4a6ed0819910c4786d03731f77d6806",
        srcjar_sha256 = "cf655c72c56222620f2f74834a0e6e1b11f9f4bdd8db3ed0205ec86b58764b86",
        deps = [
            "@com_github_alexarchambault_argonaut_shapeless_6_2_2_12",
            "@io_get_coursier_coursier_cache_2_12",
            "@io_get_coursier_coursier_core_2_12",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "io_get_coursier_coursier_cache_2_12",
        artifact = "io.get-coursier:coursier-cache_2.12:2.0.13",
        artifact_sha256 = "7cf75168d551687250cf00c3fb230d13a75b0f92d751e808a90628695f4e0db1",
        srcjar_sha256 = "1010548094425b768e4b0b78cf53b4a4d412fa9427f94f5ee444bd2e30387e73",
        deps = [
            "@io_get_coursier_coursier_util_2_12",
            "@io_github_alexarchambault_windows_ansi_windows_ansi",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "io_get_coursier_coursier_core_2_12",
        artifact = "io.get-coursier:coursier-core_2.12:2.0.13",
        artifact_sha256 = "3566da1f4a2fda82df5af19a544e1d5290da64affc307b74c9f744588f686d6a",
        srcjar_sha256 = "800aa565018c74e105b8d5cd326a6b7c0ac9c805d92655f30c60b9014bbdf616",
        deps = [
            "@io_get_coursier_coursier_util_2_12",
            "@io_github_alexarchambault_concurrent_reference_hash_map",
            "@org_scala_lang_modules_scala_xml_2_12",
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "io_get_coursier_coursier_util_2_12",
        artifact = "io.get-coursier:coursier-util_2.12:2.0.13",
        artifact_sha256 = "265bd0f75137e2010c0a434f8a6a5b2656696f43a89f83f299ae0ef289c866b7",
        srcjar_sha256 = "e5c50482f7f10e9f40368c691cbf4d77c5fbab2a7580cddbd3e4d450a39e1d68",
        deps = [
            "@org_scala_lang_modules_scala_collection_compat_2_12",
            "@org_scala_lang_scala_library",
        ],
    )
