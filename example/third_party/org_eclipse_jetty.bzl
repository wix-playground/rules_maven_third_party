load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_eclipse_jetty_jetty_alpn_client",
        artifact = "org.eclipse.jetty:jetty-alpn-client:9.4.44.v20210927",
        artifact_sha256 = "5f1a72fbb81f0e4d2e5797c2ba28a331ec979e669151fdd026adf3bbd61bdf71",
        srcjar_sha256 = "f804490d22c84820e86f955ca40a256fd2c90867132d1df907078df9fe282c65",
        deps = [
            "@org_eclipse_jetty_jetty_io",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_alpn_java_client",
        artifact = "org.eclipse.jetty:jetty-alpn-java-client:9.4.44.v20210927",
        artifact_sha256 = "72efbef86f774a35be0012245eddf45a6830363dc7ae69d54be299d6ba5238e6",
        srcjar_sha256 = "47af253f99d04feeffbf056be8491a7f8020824cc162adf8d41b2c01aa517652",
        deps = [
            "@org_eclipse_jetty_jetty_alpn_client",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_alpn_java_server",
        artifact = "org.eclipse.jetty:jetty-alpn-java-server:9.4.44.v20210927",
        artifact_sha256 = "17c25f4449d925a24034fa8d4244afc461a91226cf8cd6a18dcd1516d388243e",
        srcjar_sha256 = "f5613fb63f447c5561b47d07db6d6d50a067357f37c4068471213f1c62c3943c",
        deps = [
            "@org_eclipse_jetty_jetty_alpn_server",
            "@org_eclipse_jetty_jetty_io",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_alpn_openjdk8_client",
        artifact = "org.eclipse.jetty:jetty-alpn-openjdk8-client:9.4.44.v20210927",
        artifact_sha256 = "46812326132df5c4e3e9bcf3cfa2a61ddd84865819aca361a6734c77e8eda6eb",
        srcjar_sha256 = "16a9363248d9d9b72322c6f9bf281903b98834ab9c605278bf13deaed224d120",
        deps = [
            "@org_eclipse_jetty_jetty_alpn_client",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_alpn_openjdk8_server",
        artifact = "org.eclipse.jetty:jetty-alpn-openjdk8-server:9.4.44.v20210927",
        artifact_sha256 = "da3720f93b4ddb4a21c17f7164612ac9148aa9058bd9b18c6af76e4faa1627e4",
        srcjar_sha256 = "89911e08725d1b7fa5df79b47aa8d7d59d2b3f5d2d341121124a33362b6f7a3f",
        deps = [
            "@org_eclipse_jetty_jetty_alpn_server",
            "@org_eclipse_jetty_jetty_io",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_alpn_server",
        artifact = "org.eclipse.jetty:jetty-alpn-server:9.4.44.v20210927",
        artifact_sha256 = "d8bac1939b715e42c5a16897eb5380664405a8fd3414e3f5e24d2e13d4314d8b",
        srcjar_sha256 = "69e6d8c88591192a82b05fadbdca87ad761a0253723e02554804b289dfcb5212",
        deps = [
            "@org_eclipse_jetty_jetty_server",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_client",
        artifact = "org.eclipse.jetty:jetty-client:9.4.44.v20210927",
        artifact_sha256 = "81c335a33fea19ab71470e2b89295161f98a773fd3dfba1f4c4f9a358608090d",
        srcjar_sha256 = "14a26502d4851ae514cf97305ee7787a6ad3eb6ae2f9986240d01d3409ba932c",
        deps = [
            "@org_eclipse_jetty_jetty_http",
            "@org_eclipse_jetty_jetty_io",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_continuation",
        artifact = "org.eclipse.jetty:jetty-continuation:9.4.44.v20210927",
        artifact_sha256 = "cfb01376d77e2872a65ece6a997eff93ebc374e04db5c72a9748dca524b7e0f8",
        srcjar_sha256 = "5ad0aeaa3524c359233f85d1ef16a82f99b972dff55a35fcfb32a654ac9f1b3e",
    )


    import_external(
        name = "org_eclipse_jetty_jetty_http",
        artifact = "org.eclipse.jetty:jetty-http:9.4.44.v20210927",
        artifact_sha256 = "0a09fac4c0ea826f920cfe8d5beced61dcd8fec0eae99b88c7619609fa0dc403",
        srcjar_sha256 = "5d11274eefac7c5ab79ee8d606b55232284cb1eca7241cfce0f0c292f0805a49",
        deps = [
            "@org_eclipse_jetty_jetty_io",
            "@org_eclipse_jetty_jetty_util",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_io",
        artifact = "org.eclipse.jetty:jetty-io:9.4.44.v20210927",
        artifact_sha256 = "3c6f1105500921aa4f9687c3a1b5fd9eba4661a5f438aa089829c2ecc9726745",
        srcjar_sha256 = "35c1d56be44844a51669206c5b4ff1fd5e39cac0cde2ae10ebc514e0b087671a",
        deps = [
            "@org_eclipse_jetty_jetty_util",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_proxy",
        artifact = "org.eclipse.jetty:jetty-proxy:9.4.44.v20210927",
        artifact_sha256 = "4f2d609dc08bf50abe249a3597f5afebd523491a27d72896e6cbcd949761331a",
        srcjar_sha256 = "40084df19c6e85d093fc4235cdf074206858f1ccf4cf295c3571117b27aa15dc",
        deps = [
            "@org_eclipse_jetty_jetty_client",
            "@org_eclipse_jetty_jetty_util",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_security",
        artifact = "org.eclipse.jetty:jetty-security:9.4.44.v20210927",
        artifact_sha256 = "d7545a58dc0107035757da6538b70d2bbbc02d78e5f382ca670d258ce822a9f7",
        srcjar_sha256 = "0c0799daaab341fb2595d813367e2c6fb46074d6f3f2ecc98b368c881c544e58",
        deps = [
            "@org_eclipse_jetty_jetty_server",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_server",
        artifact = "org.eclipse.jetty:jetty-server:9.4.44.v20210927",
        artifact_sha256 = "d4f51fb02454b1c79489418f080d3409c557abca181f083881977b7a729a8f86",
        srcjar_sha256 = "24b41fe85e2aaf382fcb15d948dc1857876d44413ee73c6370fb64db8bbdb183",
        deps = [
            "@javax_servlet_javax_servlet_api//:linkable",
            "@org_eclipse_jetty_jetty_http",
            "@org_eclipse_jetty_jetty_io",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_servlet",
        artifact = "org.eclipse.jetty:jetty-servlet:9.4.44.v20210927",
        artifact_sha256 = "eb85f2cfa2cb2b809ccea0c92e33fb68542f5c0286575b48dac895daba7bd0ee",
        srcjar_sha256 = "d60c4396f6de63e70741449a7a823a391b1395910b3c189d46feb6218c480802",
        deps = [
            "@org_eclipse_jetty_jetty_security",
            "@org_eclipse_jetty_jetty_util_ajax",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_servlets",
        artifact = "org.eclipse.jetty:jetty-servlets:9.4.44.v20210927",
        artifact_sha256 = "9f70d4dc470bc2581ad182de4411ce774cd4865ca643eafc044e867f49502b43",
        srcjar_sha256 = "33d9543fc73d2e4275e6aabddc91bb78a69083ba3379c9e795a359f39909ecf7",
        deps = [
            "@org_eclipse_jetty_jetty_continuation",
            "@org_eclipse_jetty_jetty_http",
            "@org_eclipse_jetty_jetty_io",
            "@org_eclipse_jetty_jetty_util",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_util",
        artifact = "org.eclipse.jetty:jetty-util:9.4.44.v20210927",
        artifact_sha256 = "539179024520b614f62d5d83f25bea111f7b991c399e5f737fa6aa2750489079",
        srcjar_sha256 = "d797940703141bf91eed55e45f226b18b7294382689819ddc03ddbbd0b44c770",
    )


    import_external(
        name = "org_eclipse_jetty_jetty_util_ajax",
        artifact = "org.eclipse.jetty:jetty-util-ajax:9.4.44.v20210927",
        artifact_sha256 = "15aee9ad62b6af6d3f90ee37c4d190003305b4b92d9b2646fcd4e9df46c9225f",
        srcjar_sha256 = "1cf877168e068194362d7a2268441038a70e3e72500c1b1dc3dd97ec463d196d",
        deps = [
            "@org_eclipse_jetty_jetty_util",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_webapp",
        artifact = "org.eclipse.jetty:jetty-webapp:9.4.44.v20210927",
        artifact_sha256 = "b447a5dd9957f2cd414041aea46d2812bd39acc175d6d396941f8e1ce2995e96",
        srcjar_sha256 = "d19891ccb0d3c676c71a2afeaa13bc5fade973e242a0705a952a8198db86150e",
        deps = [
            "@org_eclipse_jetty_jetty_servlet",
            "@org_eclipse_jetty_jetty_xml",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_xml",
        artifact = "org.eclipse.jetty:jetty-xml:9.4.44.v20210927",
        artifact_sha256 = "5d8a77311c87015006547d23bd06e36b02212c48ca26c2b0b30b8d2ca3c6e6c3",
        srcjar_sha256 = "e1a92231a24e0d91cb1c013d2accde3dfe6f72d45bddd66c3819765c87ff1502",
        deps = [
            "@org_eclipse_jetty_jetty_util",
        ],
    )
