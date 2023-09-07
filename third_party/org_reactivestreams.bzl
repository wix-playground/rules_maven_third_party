load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_reactivestreams_reactive_streams",
        artifact = "org.reactivestreams:reactive-streams:1.0.3",
        artifact_sha256 = "1dee0481072d19c929b623e155e14d2f6085dc011529a0a0dbefc84cf571d865",
        srcjar_sha256 = "d5b4070a22c9b1ca5b9b5aa668466bcca391dbe5d5fe8311c300765c1621feba",
    )
