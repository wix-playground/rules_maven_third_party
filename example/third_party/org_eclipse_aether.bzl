load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_eclipse_aether_aether_api",
        artifact = "org.eclipse.aether:aether-api:0.9.0.M2",
        artifact_sha256 = "e220097cffad96c2963ab12652ff8833ec6f40143d509f0a2ea59d22209b6ecd",
        srcjar_sha256 = "c4d19622578da6ade56f8bff9b2119f3faf2ef4e1c2fab75fa476e60b779c433",
    )


    import_external(
        name = "org_eclipse_aether_aether_util",
        artifact = "org.eclipse.aether:aether-util:0.9.0.M2",
        artifact_sha256 = "7d62b0fdef90196ec4b2947f5973d750bfd3935785244e77cc06780131c404e9",
        srcjar_sha256 = "a1533d60307bf7ab22fafd0cde5c9b9f603ede347724a39958684eb7a0c9f92a",
        deps = [
            "@org_eclipse_aether_aether_api",
        ],
    )
