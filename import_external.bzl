load("@io_bazel_rules_scala//scala:scala_maven_import_external.bzl", "scala_maven_import_external")
load("//rules:import_external_alias.bzl", "import_external_alias")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

REPOSITORY_URLS = [
    "https://repo1.maven.org/maven2",
]

def import_external(name, artifact, **kwargs):
    if native.existing_rule(name) == None:
        srcjar_sha256_exists = kwargs.get("srcjar_sha256") != None
        snapshot_sources = kwargs.pop("snapshot_sources", 0)
        fetch_sources = srcjar_sha256_exists or snapshot_sources
        _import_external_sources(name, artifact, fetch_sources, **kwargs)

def _import_external_sources(name, artifact, fetch_sources, **kwargs):
    tags = kwargs.pop("tags", [])
    excludes = kwargs.pop("excludes", [])
    aliases = kwargs.pop("aliases", [])

    transitive_deps = kwargs.pop("transitive_closure_deps", [])
    deps = kwargs.pop("deps", [])

    if (len(excludes)):
        tags = ["excludes=%s" % ",".join(excludes)] + tags
    scala_maven_import_external(
        name = name,
        artifact = artifact,
        licenses = ["notice"],  # Apache 2.0
        fetch_sources = fetch_sources,
        server_urls = REPOSITORY_URLS,
        deps = transitive_deps if transitive_deps != [] else deps,
        tags = ["maven_coordinates=%s" % artifact] + tags,
        **kwargs
    )

    for alias in aliases:
        fixed_alias = alias[1:] if alias.startswith("@") else alias
        import_external_alias(
            name = fixed_alias,
            actual = "@" + name,
        )

def maven_archive(name, artifact):
    http_archive(
        name = name,
        urls = _convert_to_url(artifact),
        build_file_content = """filegroup(name = "unpacked", srcs = glob(["**/*"],exclude=["BUILD.bazel","WORKSPACE","*.zip","*.tar.gz"]), visibility = ["//visibility:public"])
filegroup(name = "archive", srcs = glob(["*.zip","*.tar.gz"]), visibility = ["//visibility:public"])
""",
    )

def _convert_to_url(artifact):
    parts = artifact.split(":")
    group_id_part = parts[0].replace(".", "/")
    artifact_id = parts[1]
    version = parts[2]
    packaging = "jar"
    classifier_part = ""
    if len(parts) == 4:
        packaging = parts[2]
        version = parts[3]
    elif len(parts) == 5:
        packaging = parts[2]
        classifier_part = "-" + parts[3]
        version = parts[4]

    final_name = artifact_id + "-" + version + classifier_part + "." + packaging
    url_suffix = group_id_part + "/" + artifact_id + "/" + version + "/" + final_name

    return [url + "/" + url_suffix for url in REPOSITORY_URLS]
