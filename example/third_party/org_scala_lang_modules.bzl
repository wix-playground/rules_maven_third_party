load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_scala_lang_modules_scala_collection_compat_2_12",
        artifact = "org.scala-lang.modules:scala-collection-compat_2.12:2.2.0",
        artifact_sha256 = "16de54c297c50b1a1a8b3e3801a64c7756b64e07567a1293a6bbb5377677674c",
        srcjar_sha256 = "3a7471030a534106f97e479b0370f355e9fd03a335d81162a57dfb3b18185073",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "org_scala_lang_modules_scala_parser_combinators_2_12",
        artifact = "org.scala-lang.modules:scala-parser-combinators_2.12:1.1.2",
        artifact_sha256 = "24985eb43e295a9dd77905ada307a850ca25acf819cdb579c093fc6987b0dbc2",
        srcjar_sha256 = "8fbe3fa9e748f24aa6d6868c0c2be30d41a02d20428ed5429bb57a897cb756e3",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )


    import_external(
        name = "org_scala_lang_modules_scala_xml_2_12",
        artifact = "org.scala-lang.modules:scala-xml_2.12:2.0.1",
        artifact_sha256 = "e36c0cdca62ddd4ea998db47936ab6a2f56854ec69c059a291f725ad5070538b",
        srcjar_sha256 = "fb138664d3fefb7d34c1a8ef0c8cb448ba8d6f53b9b30ad1cf9d268e293141d2",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )
