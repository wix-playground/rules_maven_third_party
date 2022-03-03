load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "commons_collections_commons_collections",
        artifact = "commons-collections:commons-collections:3.1",
        artifact_sha256 = "c1547d185ba6880bcc2da261c5f7533512b6ffdbbc1898db5b793c0cb830fcf0",
        srcjar_sha256 = "af3b19090893f662773868b4c0b7d9f5983ee89a3b2209e6704a949f236555b2",
    )
