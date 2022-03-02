load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_xmlunit_xmlunit_core",
        artifact = "org.xmlunit:xmlunit-core:2.8.3",
        artifact_sha256 = "8fb3b2f7202426d454d74da9d585b02c742825b8d997bbfd12a113d0a918dba1",
        srcjar_sha256 = "2fdc7e04e6a294024b3329796c158d55daba44c79129f1107af2f925816420f0",
        deps = [
            "@jakarta_xml_bind_jakarta_xml_bind_api",
        ],
    )


    import_external(
        name = "org_xmlunit_xmlunit_legacy",
        artifact = "org.xmlunit:xmlunit-legacy:2.8.3",
        artifact_sha256 = "ebfc62766b640fbed3d865b965a34514a6ec05048fc485db52aa4462106a99a6",
        srcjar_sha256 = "b1a62afea7b9443b5fc7b9f86f850be625f0952b79ee6d3c7216dac90c024f41",
        deps = [
            "@org_xmlunit_xmlunit_core",
        ],
        excludes = [
            "junit:junit",
        ],
    )


    import_external(
        name = "org_xmlunit_xmlunit_placeholders",
        artifact = "org.xmlunit:xmlunit-placeholders:2.8.3",
        artifact_sha256 = "0d04bc60a80bb2ef6b66321967c963df0974d75fca3d230c24c0f36ce37c0f3d",
        srcjar_sha256 = "6b0a2b0c25eedd360a0c499b2d8877b8de401beafa2f576613250b17abca5523",
        deps = [
            "@org_xmlunit_xmlunit_core",
        ],
    )
