load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "net_bytebuddy_byte_buddy",
        artifact = "net.bytebuddy:byte-buddy:1.10.5",
        artifact_sha256 = "3c9c603970bb9d68572c1aa29e9ae6b477d602922977a04bfa5f3b5465d7d1f4",
        srcjar_sha256 = "4f60d54ecffa79da4e79761b7a4706e49771b0a0daf682948327a07e39b4b207",
    )


    import_external(
        name = "net_bytebuddy_byte_buddy_agent",
        artifact = "net.bytebuddy:byte-buddy-agent:1.10.5",
        artifact_sha256 = "290c9930965ef5810ddb15baf3b3647ce952f40fa2f0af82d5f669e04ba87e5b",
        srcjar_sha256 = "2286a99c6043271d3efa6cd1190121fcbf8a9523169f78cc711420c95a120cfa",
    )
