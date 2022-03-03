load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "classworlds_classworlds",
        artifact = "classworlds:classworlds:1.1",
        artifact_sha256 = "4e3e0ad158ec60917e0de544c550f31cd65d5a97c3af1c1968bf427e4a9df2e4",
        srcjar_sha256 = "a95c7583b6bc2e50544f3c40b48390fe11e3d0fcfa95dceaa0101a03333af1c3",
    )
