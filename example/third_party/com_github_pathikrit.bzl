load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_github_pathikrit_better_files_2_12",
        artifact = "com.github.pathikrit:better-files_2.12:3.8.0",
        artifact_sha256 = "49555913cab2cf8fb81e2783331e5c3d64161786fa2f9bf52e8ef3f529697a1a",
        srcjar_sha256 = "a931ba137fb024a02ab1ca28ef61b9de9831d38bd99df30baa57d2926479bc7b",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )
