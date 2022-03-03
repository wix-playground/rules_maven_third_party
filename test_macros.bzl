load("@io_bazel_rules_scala//scala:scala.bzl", "scala_specs2_junit_test")

def specs2_unit_test(name, srcs, deps = [], runtime_deps = [], data = [], unused_dependency_checker_ignored_targets = [], **kwargs):
    scala_specs2_junit_test(
        name = name,
        srcs = srcs,
        deps = deps,
        runtime_deps = runtime_deps,
        data = data,
        size = "small",
        prefixes = ["Test"],
        suffixes = ["Test"],
        **kwargs
    )

def specs2_ite2e_test(name, srcs, deps = [], runtime_deps = [], data = [], unused_dependency_checker_ignored_targets = [], **kwargs):
    scala_specs2_junit_test(
        name = name,
        srcs = srcs,
        deps = deps,
        runtime_deps = runtime_deps,
        data = data,
        tags = ["E2E", "IT", "block-network"],
        size = "large",
        timeout = "moderate",
        prefixes = ["IT", "E2E"],
        suffixes = ["IT", "E2E"],
        **kwargs
    )
