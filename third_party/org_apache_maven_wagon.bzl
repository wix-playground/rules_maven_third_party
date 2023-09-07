load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_apache_maven_wagon_wagon_file",
        artifact = "org.apache.maven.wagon:wagon-file:1.0-beta-6",
        artifact_sha256 = "7298feeb36ff14dd933c38e62585fb9973fea32fb3c4bc5379428cb1aac5dd3c",
        srcjar_sha256 = "c96ce725e9a3e1dc002a1304e2f88a7adfd5a0cf7967b665923654099cadc78a",
        runtime_deps = [
            "@org_apache_maven_wagon_wagon_provider_api",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_http",
        artifact = "org.apache.maven.wagon:wagon-http:1.0-beta-6",
        artifact_sha256 = "98376024764658eb65e252ff2a690d9773032cafc4cc8bbb7558d0ba1c5435f4",
        srcjar_sha256 = "e89081f2bbc5c074884c77d473b342c639e03a17d9806aef024edf5727ef7a0e",
        deps = [
            "@org_apache_maven_wagon_wagon_http_shared",
            "@org_apache_maven_wagon_wagon_provider_api",
        ],
        excludes = [
            "commons-logging:commons-logging",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_http_lightweight",
        artifact = "org.apache.maven.wagon:wagon-http-lightweight:1.0-beta-6",
        artifact_sha256 = "be214032de23c6b520b79c1ccdb160948e0c67ed7c11984b7ec4ca5537867b4e",
        srcjar_sha256 = "b1f94d1995cf84413bd0a49fa5ad003066cb389fb425b44cafb91dbb85827b0a",
        deps = [
            "@org_apache_maven_wagon_wagon_http_shared",
            "@org_apache_maven_wagon_wagon_provider_api",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_http_shared",
        artifact = "org.apache.maven.wagon:wagon-http-shared:1.0-beta-6",
        artifact_sha256 = "f095c882716d49269a806685dcb256fa6a36389b2713ac56bb758bf8693565a2",
        srcjar_sha256 = "2772ddff54f12e10f3b91969463b045255a1a40a246689daf9ffadd17934997f",
        deps = [
            "@nekohtml_nekohtml",
            "@nekohtml_xercesMinimal",
            "@org_apache_maven_wagon_wagon_provider_api",
        ],
        excludes = [
            "commons-httpclient:commons-httpclient",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_provider_api",
        artifact = "org.apache.maven.wagon:wagon-provider-api:1.0-beta-6",
        artifact_sha256 = "e116f32edcb77067289a3148143f2c0c97b27cf9a1342f8108ee37dec4868861",
        srcjar_sha256 = "b6d1e11b976d085d6796ccb11cca3c68e7857786294e8298760f8673e5c9c9ac",
        deps = [
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_ssh",
        artifact = "org.apache.maven.wagon:wagon-ssh:1.0-beta-6",
        artifact_sha256 = "5465b4a6192d2f691b211f5bd17518e7949177eff040ea709f3acfce79ce1454",
        srcjar_sha256 = "fd81f490eca43666fdf85929307ce5dce901c8d61c23d0da064898b865fe11e5",
        deps = [
            "@com_jcraft_jsch",
            "@org_apache_maven_wagon_wagon_provider_api",
            "@org_apache_maven_wagon_wagon_ssh_common",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_ssh_common",
        artifact = "org.apache.maven.wagon:wagon-ssh-common:1.0-beta-6",
        artifact_sha256 = "456a1abdab3c3984812f6a25e3f3e21aa6aa6f8a5d0db98d32cbc20f95c301c0",
        srcjar_sha256 = "4190e7c4944a2a40403ed71be7936174264ebd22c006eac37e505943348fc0a2",
        deps = [
            "@org_apache_maven_wagon_wagon_provider_api",
            "@org_codehaus_plexus_plexus_interactivity_api",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_ssh_external",
        artifact = "org.apache.maven.wagon:wagon-ssh-external:1.0-beta-6",
        artifact_sha256 = "3251974da3988d1db453954a66541aa11d2224b5509065c6fec69912793707ab",
        srcjar_sha256 = "fafad083994e9c7adafef7eb089ceac5312bb69e8b8811116c89ff7742b0193d",
        deps = [
            "@org_apache_maven_wagon_wagon_ssh_common",
            "@org_codehaus_plexus_plexus_utils",
        ],
        runtime_deps = [
            "@org_apache_maven_wagon_wagon_provider_api",
        ],
    )


    import_external(
        name = "org_apache_maven_wagon_wagon_webdav_jackrabbit",
        artifact = "org.apache.maven.wagon:wagon-webdav-jackrabbit:1.0-beta-6",
        artifact_sha256 = "1337191fadb43c336dafc4537116b400800ad1a8e4b3a3ef945a16b640f242db",
        srcjar_sha256 = "d2457f12618131db61b3623780d67a941c721e7618c3fef6484591da830dbe09",
        runtime_deps = [
            "@org_apache_jackrabbit_jackrabbit_webdav",
            "@org_apache_maven_wagon_wagon_http_shared",
            "@org_apache_maven_wagon_wagon_provider_api",
            "@org_slf4j_slf4j_nop",
        ],
        excludes = [
            "commons-logging:commons-logging",
        ],
    )
