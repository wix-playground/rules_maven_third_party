load("@rules_maven_third_party//rules:maven_specs.bzl", "wix")

MANAGED_DEPS = [
    wix.artifact(group = "junit", artifact = "junit", version = "4.13.2"),
]
