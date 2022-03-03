load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_googlecode_javaewah_JavaEWAH",
        artifact = "com.googlecode.javaewah:JavaEWAH:1.1.7",
        artifact_sha256 = "3ecf8b2c602314341f5a2ace171ed04fc86f2d4ddf762180656e9b71134ae68f",
        srcjar_sha256 = "e4ee176218d910edd5d3d06e08bd5385ba312886bcf4574f12f59dbee37fc5a2",
    )
