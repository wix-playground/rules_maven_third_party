load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_codehaus_plexus_plexus_archiver",
        artifact = "org.codehaus.plexus:plexus-archiver:3.4",
        artifact_sha256 = "3c6611c98547dbf3f5125848c273ba719bc10df44e3f492fa2e302d6135a6ea5",
        srcjar_sha256 = "1887e8269928079236c9e1a75af5b5e256f4bfafaaed18da5c9c84faf5b26a91",
        deps = [
            "@commons_io_commons_io",
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
        name = "org_codehaus_plexus_plexus_component_annotations",
        artifact = "org.codehaus.plexus:plexus-component-annotations:1.7.1",
        artifact_sha256 = "a7fee9435db716bff593e9fb5622bcf9f25e527196485929b0cd4065c43e61df",
        srcjar_sha256 = "18999359e8c1c5eb1f17a06093ceffc21f84b62b4ee0d9ab82f2e10d11049a78",
        excludes = [
            "junit:junit",
        ],
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
        artifact = "org.codehaus.plexus:plexus-interpolation:1.11",
        artifact_sha256 = "fd9507feb858fa620d1b4aa4b7039fdea1a77e09d3fd28cfbddfff468d9d8c28",
        srcjar_sha256 = "92e960ff19db5b1f8cd399d20bd52c2dd3c81c080029f2a9b7c77e077d0b0d83",
    )


    import_external(
        name = "org_codehaus_plexus_plexus_io",
        artifact = "org.codehaus.plexus:plexus-io:2.7.1",
        artifact_sha256 = "20aa9dd74536ad9ce65d1253b5c4386747483a7a65c48008c9affb51854539cf",
        deps = [
            "@commons_io_commons_io",
            "@org_codehaus_plexus_plexus_utils",
        ],
    )


    import_external(
        name = "org_codehaus_plexus_plexus_utils",
        artifact = "org.codehaus.plexus:plexus-utils:3.0.24",
        artifact_sha256 = "83ee748b12d06afb0ad4050a591132b3e8025fbb1990f1ed002e8b73293e69b4",
        srcjar_sha256 = "ec01853bc765eddc0accf19993482c14ab0aa59dccec4efd361ef23649153935",
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
