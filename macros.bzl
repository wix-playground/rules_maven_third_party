load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def resources(
        name = "resources",
        resources = None,
        resource_extra_strip_prefix = None,
        runtime_deps = [],
        testonly = False,
        tags = [],
        visibility = None):
    if resources == None:
        resources = native.glob(["**"], exclude = ["BUILD", "BUILD.*"])

    resource_strip_prefix = "%s/" % native.package_name()
    if resource_extra_strip_prefix != None:
        resource_strip_prefix += "%s/" % resource_extra_strip_prefix

    native.java_library(
        name = name,
        resources = resources,
        resource_strip_prefix = resource_strip_prefix,
        runtime_deps = runtime_deps,
        testonly = testonly,
        tags = tags,
        visibility = visibility,
    )
