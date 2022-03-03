load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_fasterxml_jackson_module_jackson_module_paranamer",
        artifact = "com.fasterxml.jackson.module:jackson-module-paranamer:2.9.9",
        artifact_sha256 = "6f57a6df1b99cf84cb12e117fa06e134015ae896c0e27b9494136fabc3a1f508",
        srcjar_sha256 = "fbaa6779d8fa55de5a26910f92b57ffe346e2fb02860dc56dd963fcea58801b4",
        deps = [
            "@com_fasterxml_jackson_core_jackson_databind",
            "@com_thoughtworks_paranamer_paranamer",
        ],
    )


    import_external(
        name = "com_fasterxml_jackson_module_jackson_module_scala_2_12",
        artifact = "com.fasterxml.jackson.module:jackson-module-scala_2.12:2.9.9",
        artifact_sha256 = "307e42a438e69b6bd7fe788f7bd39dab19a90c70ae1dc4c134692f85ecfa9d65",
        srcjar_sha256 = "a166bfcd60311b5c00a9b694538bb3d1ea53c4df64f1a5aa09992d0e82224489",
        deps = [
            "@com_fasterxml_jackson_core_jackson_annotations",
            "@com_fasterxml_jackson_core_jackson_core",
            "@com_fasterxml_jackson_core_jackson_databind",
            "@com_fasterxml_jackson_module_jackson_module_paranamer",
            "@org_scala_lang_scala_library",
        ],
    )
