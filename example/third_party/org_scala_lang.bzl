load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_scala_lang_scala_compiler",
        artifact = "org.scala-lang:scala-compiler:2.12.13",
        artifact_sha256 = "ea971e004e2f15d3b7569eee8b559f220e23b9993e688bbe986f97938d1dc9f9",
        srcjar_sha256 = "2229b671c1f481ef52bcba19bab859330366fa915cabca7e06c01add02e5cc21",
        deps = [
            "@org_scala_lang_modules_scala_xml_2_12",
            "@org_scala_lang_scala_library",
            "@org_scala_lang_scala_reflect",
        ],
    )


    import_external(
        name = "org_scala_lang_scala_library",
        artifact = "org.scala-lang:scala-library:2.12.13",
        artifact_sha256 = "1bb415cff43f792636556a1137b213b192ab0246be003680a3b006d01235dd89",
        srcjar_sha256 = "d299cc22829c08bc595a1d4378d7ad521babb6871ca2eab623d55b80c9307653",
    )


    import_external(
        name = "org_scala_lang_scala_reflect",
        artifact = "org.scala-lang:scala-reflect:2.12.13",
        artifact_sha256 = "2bd46318d87945e72eb186a7b5ea496c43cf8f0aabc6ff11b3e7962f8635e669",
        srcjar_sha256 = "1226f12c7d368b76e8ed43fad1cf93a5516efaaa0f75f865fddb818bb06d9256",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )
