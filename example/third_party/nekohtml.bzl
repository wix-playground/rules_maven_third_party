load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "nekohtml_nekohtml",
        artifact = "nekohtml:nekohtml:1.9.6.2",
        artifact_sha256 = "fdff6cfa9ed9cc911c842a5d2395f209ec621ef1239d46810e9e495809d3ae09",
        srcjar_sha256 = "d1a7d483aedee77a4a4e3a66f2589780552ca150a8aafcb114325655d72c911b",
        excludes = [
            "xerces:xercesImpl",
        ],
    )


    import_external(
        name = "nekohtml_xercesMinimal",
        artifact = "nekohtml:xercesMinimal:1.9.6.2",
        artifact_sha256 = "95b8b357d19f63797dd7d67622fd3f18374d64acbc6584faba1c7759a31e8438",
    )
