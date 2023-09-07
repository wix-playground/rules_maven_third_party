load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_hamcrest_hamcrest",
        artifact = "org.hamcrest:hamcrest:2.2",
        artifact_sha256 = "5e62846a89f05cd78cd9c1a553f340d002458380c320455dd1f8fc5497a8a1c1",
        srcjar_sha256 = "f49e697dbc70591f91a90dd7f741f5780f53f63f34a416d6a9879499d4d666af",
    )


    import_external(
        name = "org_hamcrest_hamcrest_core",
        artifact = "org.hamcrest:hamcrest-core:2.2",
        artifact_sha256 = "094f5d92b4b7d9c8a2bf53cc69d356243ae89c3499457bcb4b92f7ed3bf95879",
        srcjar_sha256 = "35e1bf1710a410384209b4448073747454e3320afac1cc1de73e5d30b0136c7a",
        deps = [
            "@org_hamcrest_hamcrest",
        ],
    )
