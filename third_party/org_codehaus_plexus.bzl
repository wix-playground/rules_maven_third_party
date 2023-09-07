load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_codehaus_plexus_plexus_archiver",
        artifact = "org.codehaus.plexus:plexus-archiver:4.2.6",
        artifact_sha256 = "5bd081cc9a00b1c2c0396c1fea1926e28eba322d7e68bf3c4d9c0bd408de6ff8",
        srcjar_sha256 = "2904cdcdbd3e24fa68f432564d8e0634a3d1b2e9c059bde0913ce3bd91aeff75",
        deps = [
            "@org_apache_commons_commons_compress",
            "@org_codehaus_plexus_plexus_io",
            "@org_codehaus_plexus_plexus_utils",
            "@org_iq80_snappy_snappy",
        ],
        runtime_deps = [
            "@org_tukaani_xz",
        ],
    )


    import_external(
        name = "org_codehaus_plexus_plexus_classworlds",
        artifact = "org.codehaus.plexus:plexus-classworlds:2.4",
        artifact_sha256 = "259d528a29722cab6349d7e7d432e3fd4877c087ffcb04985a6612e97023bba8",
        srcjar_sha256 = "2949b80b5a0d5d828a4a4667f98c6365e61980b457e1894d3beb977910ebeef9",
    )


    import_external(
        name = "org_codehaus_plexus_plexus_component_annotations",
        artifact = "org.codehaus.plexus:plexus-component-annotations:1.5.5",
        artifact_sha256 = "4df7a6a7be64b35bbccf60b5c115697f9ea3421d22674ae67135dde375fcca1f",
        srcjar_sha256 = "527768d357304e0ad56b74ca77f27ba28b4a456680450ef45a30bfaf613469e6",
    )


    import_external(
        name = "org_codehaus_plexus_plexus_container_default",
        artifact = "org.codehaus.plexus:plexus-container-default:1.0-alpha-9-stable-1",
        artifact_sha256 = "7c758612888782ccfe376823aee7cdcc7e0cdafb097f7ef50295a0b0c3a16edf",
        srcjar_sha256 = "330e1bf490259c64fd6b27097adb8d41159500f849743b8680f3515044483d62",
        deps = [
            "@classworlds_classworlds",
            "@junit_junit",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_codehaus_plexus_plexus_interactivity_api",
        artifact = "org.codehaus.plexus:plexus-interactivity-api:1.0-alpha-4",
        artifact_sha256 = "4f60eb379f93d8b616bc3b4d299f466bc54fcced959f7ad082dae78b89d6a3f0",
        srcjar_sha256 = "2eae2dc145b8dca70671f4607255b5419b6609741c753b925debcef598d56206",
        deps = [
            "@classworlds_classworlds",
        ],
        excludes = [
            "org.codehaus.plexus:plexus-container-default",
            "plexus:plexus-utils",
        ],
    )


    import_external(
        name = "org_codehaus_plexus_plexus_interpolation",
        artifact = "org.codehaus.plexus:plexus-interpolation:1.26",
        artifact_sha256 = "b3b5412ce17889103ea564bcdfcf9fb3dfa540344ffeac6b538a73c9d7182662",
        srcjar_sha256 = "048ec9a9ae5fffbe8fa463824b852ea60d9cebd7397446f6a516fcde05863366",
    )


    import_external(
        name = "org_codehaus_plexus_plexus_io",
        artifact = "org.codehaus.plexus:plexus-io:3.2.0",
        artifact_sha256 = "15cf8cbd9e014b7156482bbb48e515613158bdd9b4b908d21e6b900f7876f6ff",
        srcjar_sha256 = "cc6aeb4522a42c1eb441336e6953069ace6a901d85ce251c20eda7baded9a10b",
        deps = [
            "@commons_io_commons_io",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_codehaus_plexus_plexus_utils",
        artifact = "org.codehaus.plexus:plexus-utils:3.4.1",
        artifact_sha256 = "52d85e04b3918722af11d12855b4a8257df96a0e76c8f4e3852e6faa851f357b",
        srcjar_sha256 = "a4399a142c0e639a7b20d621204e0805d1fb287f4fd70a26b2fe60a3b0b56192",
    )


    import_external(
        name = "org_codehaus_plexus_plexus_velocity",
        artifact = "org.codehaus.plexus:plexus-velocity:1.1.8",
        artifact_sha256 = "36b3ea3d0cef03f36bd2c4e0f34729c3de80fd375059bdccbf52b10a42c6ec2c",
        srcjar_sha256 = "906065102c989b1a82ab0871de1489381835af84cdb32c668c8af59d8a7767fe",
        deps = [
            "@commons_collections_commons_collections",
            "@org_codehaus_plexus_plexus_container_default",
        ],
        excludes = [
            "velocity:velocity",
            "velocity:velocity-api",
        ],
    )
