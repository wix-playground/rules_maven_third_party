workspace(name = "rules_maven_third_party")

local_repository(
    name = "ignore_example_workspace",
    path = "example",
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

skylib_version = "1.0.3"

http_archive(
    name = "com_google_protobuf",
    sha256 = "cf754718b0aa945b00550ed7962ddc167167bd922b842199eeb6505e6f344852",
    strip_prefix = "protobuf-3.11.3",
    urls = [
        "https://mirror.bazel.build/github.com/protocolbuffers/protobuf/archive/v3.11.3.tar.gz",
        "https://github.com/protocolbuffers/protobuf/archive/v3.11.3.tar.gz",
    ],
)

http_archive(
    name = "rules_proto",
    sha256 = "8e7d59a5b12b233be5652e3d29f42fba01c7cbab09f6b3a8d0a57ed6d1e9a0da",
    strip_prefix = "rules_proto-7e4afce6fe62dbff0a4a03450143146f9f2d7488",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/7e4afce6fe62dbff0a4a03450143146f9f2d7488.tar.gz",
        "https://github.com/bazelbuild/rules_proto/archive/7e4afce6fe62dbff0a4a03450143146f9f2d7488.tar.gz",
    ],
)

http_archive(
    name = "rules_python",
    sha256 = "e5470e92a18aa51830db99a4d9c492cc613761d5bdb7131c04bd92b9834380f6",
    strip_prefix = "rules_python-4b84ad270387a7c439ebdccfd530e2339601ef27",
    urls = ["https://github.com/bazelbuild/rules_python/archive/4b84ad270387a7c439ebdccfd530e2339601ef27.tar.gz"],
)

http_archive(
    name = "zlib",
    build_file = "@com_google_protobuf//third_party:zlib.BUILD",
    sha256 = "c3e5e9fdd5004dcb542feda5ee4f0ff0744628baf8ed2dd5d66f8ca1197cb1a1",
    strip_prefix = "zlib-1.2.11",
    urls = [
        "https://mirror.bazel.build/zlib.net/zlib-1.2.11.tar.gz",
        "https://zlib.net/zlib-1.2.11.tar.gz",
    ],
)

http_archive(
    name = "bazel_skylib",
    sha256 = "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c",
    type = "tar.gz",
    url = "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(skylib_version, skylib_version),
)

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = "265adb4c0121024f13772815c674d793d7c949739d0133ed61cfd68ce73a49a5",
    strip_prefix = "rules_scala-56bfe4f3cb79e1d45a3b64dde59a3773f67174e2",
    url = "https://github.com/bazelbuild/rules_scala/archive/56bfe4f3cb79e1d45a3b64dde59a3773f67174e2.tar.gz",
)

load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")

scala_config(
    enable_compiler_dependency_tracking = True,
    scala_version = "2.12.13",
)

load("@io_bazel_rules_scala//scala:scala.bzl", "rules_scala_setup")

rules_scala_setup()

register_toolchains("//:custom_scala_toolchain", ":testing_toolchain")

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

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
