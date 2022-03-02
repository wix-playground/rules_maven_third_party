load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_jayway_jsonpath_json_path",
        artifact = "com.jayway.jsonpath:json-path:2.6.0",
        artifact_sha256 = "c175df1eb0cb14dc5adc9f19a1566c7d16d7e419c48dc1771aec8d1852790f4b",
        srcjar_sha256 = "e09d15cfa82506433929ba273735b023d5f2a99c962e68b5c904a5ef04e60a37",
        deps = [
            "@net_minidev_json_smart",
            "@org_slf4j_slf4j_api",
        ],
        excludes = [
            "org.ow2.asm:asm",
        ],
    )
