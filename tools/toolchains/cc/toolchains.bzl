load("@hermetic_cc_toolchain//toolchain:defs.bzl", zig_toolchains = "toolchains")

def cc_toolchains():
    zig_toolchains()

    native.register_toolchains(
        "@zig_sdk//toolchain:linux_amd64_gnu.2.28",
        "@zig_sdk//toolchain:linux_arm64_gnu.2.28",
        "@zig_sdk//toolchain:darwin_amd64",
        "@zig_sdk//toolchain:darwin_arm64",
    )
