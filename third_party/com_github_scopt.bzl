load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_github_scopt_scopt_2_12",
        artifact = "com.github.scopt:scopt_2.12:3.7.1",
        artifact_sha256 = "41c77bfc41a9492a9aa0760fa873cf99289595ff97fe761e28726e00b44f9e6a",
        srcjar_sha256 = "04b53da21b8d623dabcae8618075713ae01a731e201df639ab2eec279b232014",
        deps = [
            "@org_scala_lang_scala_library",
        ],
    )
