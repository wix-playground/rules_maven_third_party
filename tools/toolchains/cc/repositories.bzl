load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

HERMETIC_CC_TOOLCHAIN_VERSION = "v3.0.1"

def cc_repositories():
    http_archive(
        name = "hermetic_cc_toolchain",
        sha256 = "3bc6ec127622fdceb4129cb06b6f7ab098c4d539124dde96a6318e7c32a53f7a",
        urls = [
            "https://mirror.bazel.build/github.com/uber/hermetic_cc_toolchain/releases/download/{0}/hermetic_cc_toolchain-{0}.tar.gz".format(HERMETIC_CC_TOOLCHAIN_VERSION),
            "https://github.com/uber/hermetic_cc_toolchain/releases/download/{0}/hermetic_cc_toolchain-{0}.tar.gz".format(HERMETIC_CC_TOOLCHAIN_VERSION),
        ],
    )
