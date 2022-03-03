load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "commons_cli_commons_cli",
        artifact = "commons-cli:commons-cli:1.2",
        artifact_sha256 = "e7cd8951956d349b568b7ccfd4f5b2529a8c113e67c32b028f52ffda371259d9",
        srcjar_sha256 = "b7017c4e576504decd4230f54d79d1140060d57d4d0a65fd3746c0758450a6f1",
        excludes = [
            "commons-lang:commons-lang",
            "commons-logging:commons-logging",
        ],
    )
