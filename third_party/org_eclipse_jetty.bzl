load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

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
        artifact = "org.eclipse.jetty:jetty-http:9.4.43.v20210629",
        artifact_sha256 = "bcbf228fed3984962be41910193fb8d6fe3fbaa8a39b8cc6bc7c20e1e82e0937",
        srcjar_sha256 = "6cd7b79461b3f34fd6fd2ff30365b87b50543203f90fb0348c6fe633ca1353e6",
        deps = [
            "@org_eclipse_jetty_jetty_io",
            "@org_eclipse_jetty_jetty_util",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_io",
        artifact = "org.eclipse.jetty:jetty-io:9.4.43.v20210629",
        artifact_sha256 = "0f37f12273dbd0ae216549e425203f51f63cbf4bfef3f994d832d24730f03f0b",
        srcjar_sha256 = "5d037cbd5e1d7ccb53cf8537525c93b192f7bb4af5b12ae16e0a5037c1c6f457",
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
        artifact = "org.eclipse.jetty:jetty-server:9.4.43.v20210629",
        artifact_sha256 = "7b64a7e1cbd249c0e510d6cda545dbf9393d41d9a904f9243fdaf64f98cc60e8",
        srcjar_sha256 = "27d1ab80e3199f249a1799cb40c15321bf747563785d8f81a72fada57c31ed2b",
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
        artifact = "org.eclipse.jetty:jetty-webapp:9.4.43.v20210629",
        artifact_sha256 = "1d995f459e8cf9418a19028cd5bd5e011dec1a3ab742380a37988f02ad80e95b",
        srcjar_sha256 = "5770ed5896e4f6c872579fdbf8d4f6fed76b8a8be6434243ad5c53692a509c91",
        deps = [
            "@org_eclipse_jetty_jetty_servlet",
            "@org_eclipse_jetty_jetty_xml",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_jetty_xml",
        artifact = "org.eclipse.jetty:jetty-xml:9.4.43.v20210629",
        artifact_sha256 = "8e66e09a3e0e2e5c8e8cf4658f2237c6a873aed511e4610aba121701192833e7",
        srcjar_sha256 = "1a7e10ccaa69b076ff8a0e18090cb9bab1cb8a520436b6131b166cb4808c0bcf",
        deps = [
            "@org_eclipse_jetty_jetty_util",
        ],
    )
