load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "com_github_alexarchambault_argonaut_shapeless_6_2_2_12",
        artifact = "com.github.alexarchambault:argonaut-shapeless_6.2_2.12:1.2.0",
        artifact_sha256 = "6d79ba3d7bf05f821b4b18a22cc628d2c75498e4dda5de9695a9f7198096cde0",
        srcjar_sha256 = "b9d0a4798f994c9cfeffa02069f105caa88aad3d44e89c2f87b80acabc8f377c",
        deps = [
            "@com_chuusai_shapeless_2_12",
            "@io_argonaut_argonaut_2_12",
            "@org_scala_lang_scala_library",
        ],
    )
