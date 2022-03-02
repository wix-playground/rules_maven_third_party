load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "io_github_alexarchambault_windows_ansi_windows_ansi",
        artifact = "io.github.alexarchambault.windows-ansi:windows-ansi:0.0.3",
        artifact_sha256 = "ad622c46395c28246f6ac3e1f4970b370e3ff19daa41af94ecfb61697cec4e92",
        srcjar_sha256 = "6285b77411e1b35b461d40f3fa8dd8458e15d1da8a7835c3903e264a82665b01",
        deps = [
            "@org_fusesource_jansi_jansi",
        ],
    )
