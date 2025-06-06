workspace(name = "rules_maven_third_party")

local_repository(
    name = "ignore_example_workspace",
    path = "example",
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# Use Zig for C++ toolchains
load("//tools/toolchains/cc:repositories.bzl", "cc_repositories")

cc_repositories()

load("//tools/toolchains/cc:toolchains.bzl", "cc_toolchains")

cc_toolchains()

http_archive(
    name = "rules_java",
    sha256 = "8daa0e4f800979c74387e4cd93f97e576ec6d52beab8ac94710d2931c57f8d8b",
    url = "https://github.com/bazelbuild/rules_java/releases/download/8.9.0/rules_java-8.9.0.tar.gz",
)

load("@rules_java//java:rules_java_deps.bzl", "rules_java_dependencies")

rules_java_dependencies()

load("@rules_java//java:repositories.bzl", "rules_java_toolchains")

rules_java_toolchains()

http_archive(
    name = "rules_python",
    sha256 = "9c6e26911a79fbf510a8f06d8eedb40f412023cf7fa6d1461def27116bff022c",
    strip_prefix = "rules_python-1.1.0",
    url = "https://github.com/bazelbuild/rules_python/releases/download/1.1.0/rules_python-1.1.0.tar.gz",
)

load("@rules_python//python:repositories.bzl", "py_repositories")

py_repositories()

# Loaded by rules_java
load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

http_archive(
    name = "rules_proto",
    sha256 = "14a225870ab4e91869652cfd69ef2028277fc1dc4910d65d353b62d6e0ae21f4",
    strip_prefix = "rules_proto-7.1.0",
    url = "https://github.com/bazelbuild/rules_proto/releases/download/7.1.0/rules_proto-7.1.0.tar.gz",
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies")

rules_proto_dependencies()

load("@rules_proto//proto:toolchains.bzl", "rules_proto_toolchains")

rules_proto_toolchains()

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = "3b00fa0b243b04565abb17d3839a5f4fa6cc2cac571f6db9f83c1982ba1e19e5",
    strip_prefix = "rules_scala-6.5.0",
    url = "https://github.com/bazelbuild/rules_scala/releases/download/v6.5.0/rules_scala-v6.5.0.tar.gz",
)

load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")

scala_config(
    enable_compiler_dependency_tracking = True,
    scala_version = "2.12.13",
)

load("@io_bazel_rules_scala//scala:scala.bzl", "rules_scala_setup")

rules_scala_setup()

register_toolchains("//:custom_scala_toolchain", ":testing_toolchain")

load("//:third_party.bzl", "managed_third_party_dependencies")

managed_third_party_dependencies()

load(":managed_artifacts.bzl", "MANAGED_DEPS")
load("//rules:managed_third_party.bzl", "managed_third_party")
load(":import_external.bzl", "REPOSITORY_URLS")

managed_third_party(
    artifacts = MANAGED_DEPS,
    import_external_macro_name = "import_external",
    import_external_rule_path = ":import_external.bzl",
    repository_urls = REPOSITORY_URLS,
)
