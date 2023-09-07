load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "net_minidev_accessors_smart",
        artifact = "net.minidev:accessors-smart:2.4.7",
        artifact_sha256 = "ef5103429f101f7e3ff62f3a182342720439eaea43d2eed3119bba770bb202a9",
        srcjar_sha256 = "85b1dee0c967b7a5515dc5dd381cded097aae594a37d6814a757289c18bdd487",
    )


    import_external(
        name = "net_minidev_json_smart",
        artifact = "net.minidev:json-smart:2.4.7",
        artifact_sha256 = "28c17ed16ac22e6845743fd1e84321edf5d7735fc216e44ee269d106bf3d8146",
        srcjar_sha256 = "339532d8b3688a4b93fb00b92fd01d48138d21e5e5324dc6a28350da9f7b666f",
        deps = [
            "@net_minidev_accessors_smart",
        ],
    )
