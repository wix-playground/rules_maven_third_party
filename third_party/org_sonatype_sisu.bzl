load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_sonatype_sisu_sisu_guice_no_aop",
        artifact = "org.sonatype.sisu:sisu-guice:jar:no_aop:3.1.0",
        artifact_sha256 = "4b76079f35407e5682aac1ecbe67afd5f430ae619044a9d6a413666a45750c25",
        srcjar_sha256 = "df7c4edf6937852f893f051560f11d96ed7c93263b0bfa381039dfc7b3adad59",
        deps = [
            "@aopalliance_aopalliance",
            "@javax_inject_javax_inject",
        ],
        excludes = [
            "org.sonatype.sisu:sisu-guava",
        ],
    )
