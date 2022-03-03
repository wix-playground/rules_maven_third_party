load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "jakarta_xml_bind_jakarta_xml_bind_api",
        artifact = "jakarta.xml.bind:jakarta.xml.bind-api:2.3.3",
        artifact_sha256 = "c04539f472e9a6dd0c7685ea82d677282269ab8e7baca2e14500e381e0c6cec5",
        srcjar_sha256 = "6da17f357f2e37a53ddeefb2d7445c1da550e50119fe87e0b4c7f275dc48b3cb",
        deps = [
            "@jakarta_activation_jakarta_activation_api",
        ],
    )
