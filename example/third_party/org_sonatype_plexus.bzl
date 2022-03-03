load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_sonatype_plexus_plexus_cipher",
        artifact = "org.sonatype.plexus:plexus-cipher:1.4",
        artifact_sha256 = "5a15fdba22669e0fdd06e10dcce6320879e1f7398fbc910cd0677b50672a78c4",
        srcjar_sha256 = "35291f96baa7d430cac96e15862e288527e08689b6f30c4f39482f19962ea540",
    )


    import_external(
        name = "org_sonatype_plexus_plexus_sec_dispatcher",
        artifact = "org.sonatype.plexus:plexus-sec-dispatcher:1.3",
        artifact_sha256 = "3b0559bb8432f28937efe6ca193ef54a8506d0075d73fd7406b9b116c6a11063",
        srcjar_sha256 = "4119c5a2968affbea144bc42cd7362fd0e3ea262df01afb8c13ebc4e1f514849",
        deps = [
            "@org_codehaus_plexus_plexus_utils",
            "@org_sonatype_plexus_plexus_cipher",
        ],
    )
