load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "net_javacrumbs_json_unit_json_unit_core",
        artifact = "net.javacrumbs.json-unit:json-unit-core:2.28.0",
        artifact_sha256 = "e8064c3ec934c300e3535618fff22f34dd1220cee678b0dae746b5427faef7d5",
        srcjar_sha256 = "c2bb96b71cf6dd3d17e430c1c86097e69c8e0acf640afc8776ee0d6d58732013",
        deps = [
            "@org_hamcrest_hamcrest_core",
        ],
    )
