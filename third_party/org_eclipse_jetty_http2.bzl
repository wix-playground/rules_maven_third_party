load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_eclipse_jetty_http2_http2_common",
        artifact = "org.eclipse.jetty.http2:http2-common:9.4.44.v20210927",
        artifact_sha256 = "fcddc2fe3dbd8adca875d92b62b1b8a135c66958320fc08ff128f56993e5a85a",
        srcjar_sha256 = "f72bbc4d23941f2f1a3744202ad3fb24236789af9dfe975af925bab1c825f612",
        deps = [
            "@org_eclipse_jetty_http2_http2_hpack",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_http2_http2_hpack",
        artifact = "org.eclipse.jetty.http2:http2-hpack:9.4.44.v20210927",
        artifact_sha256 = "0d33ad0094aeb7418d89bdad6e1aa5d81e805abfedcb85d0255146065246e372",
        srcjar_sha256 = "03309134b09d3e1a2fee6c60e9d207b63bfda3bdae23a64c6e9d2fb92f72478a",
        deps = [
            "@org_eclipse_jetty_jetty_http",
            "@org_eclipse_jetty_jetty_io",
            "@org_eclipse_jetty_jetty_util",
        ],
    )


    import_external(
        name = "org_eclipse_jetty_http2_http2_server",
        artifact = "org.eclipse.jetty.http2:http2-server:9.4.44.v20210927",
        artifact_sha256 = "9c334eac3710a3dd23319c3316ba41dc653d2867c4134a105eeb12c6a0521b23",
        srcjar_sha256 = "8a5a8496708db8d81dd0c5c084bd4ff95c5c1d12a213ae2ade62777e688e8989",
        deps = [
            "@org_eclipse_jetty_http2_http2_common",
            "@org_eclipse_jetty_jetty_server",
        ],
    )
