load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_github_jknack_handlebars",
        artifact = "com.github.jknack:handlebars:4.3.0",
        artifact_sha256 = "9441bb30635ae1db3a73d793accfe91ed4c2a4edec39750557f11f1debbc8eb7",
        srcjar_sha256 = "8f60a956d77ec362ee6b4407927a613022dded21a230f498f9c023e025082345",
        deps = [
            "@org_slf4j_slf4j_api",
        ],
        excludes = [
            "org.mozilla:rhino",
        ],
    )


    import_external(
        name = "com_github_jknack_handlebars_helpers",
        artifact = "com.github.jknack:handlebars-helpers:4.3.0",
        artifact_sha256 = "7d3298df331bb207c3c29a66cbcec9cc18a0e34dae04eb7ab6956111ffcf6b5e",
        srcjar_sha256 = "f5cb0a3af4e13a4fb108b36974181bda2c36ec81f82f79785ece214225872529",
        deps = [
            "@com_github_jknack_handlebars",
            "@org_apache_commons_commons_lang3",
        ],
        excludes = [
            "org.mozilla:rhino",
        ],
    )
