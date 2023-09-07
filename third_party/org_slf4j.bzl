load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "org_slf4j_jcl_over_slf4j",
        artifact = "org.slf4j:jcl-over-slf4j:1.7.25",
        artifact_sha256 = "5e938457e79efcbfb3ab64bc29c43ec6c3b95fffcda3c155f4a86cc320c11e14",
        srcjar_sha256 = "3c69bcf47d62cfb115312f1d99df4b5ebfb72b9809f06139d4df3e21209afed5",
        deps = [
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_slf4j_slf4j_api",
        artifact = "org.slf4j:slf4j-api:1.7.25",
        artifact_sha256 = "18c4a0095d5c1da6b817592e767bb23d29dd2f560ad74df75ff3961dbde25b79",
        srcjar_sha256 = "c4bc93180a4f0aceec3b057a2514abe04a79f06c174bbed910a2afb227b79366",
    )


    import_external(
        name = "org_slf4j_slf4j_jdk14",
        artifact = "org.slf4j:slf4j-jdk14:1.7.25",
        artifact_sha256 = "9b8b9b8279959b17e71432d40b8cf4175c761c3bc6ebc2c7ec0f2ae8ff223feb",
        srcjar_sha256 = "a47604a65ecdc62b366253988c61f9c1fec2d24cdadf8099a2cd38f6875c6758",
        deps = [
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_slf4j_slf4j_nop",
        artifact = "org.slf4j:slf4j-nop:1.5.3",
        artifact_sha256 = "3a89e932fa82a0c1ae476f0bee977571ea3fb0f31d3718bc53467f2033336379",
        srcjar_sha256 = "b209a1193a5fab20c28818fb86e93e54803bb7363f7c32d52be8186568504416",
        deps = [
            "@org_slf4j_slf4j_api",
        ],
    )


    import_external(
        name = "org_slf4j_slf4j_simple",
        artifact = "org.slf4j:slf4j-simple:1.7.25",
        artifact_sha256 = "0966e86fffa5be52d3d9e7b89dd674d98a03eed0a454fbaf7c1bd9493bd9d874",
        srcjar_sha256 = "2cfa254e77c6f41bdcd8500c61c0f6b9959de66835d2b598102d38c2a807f367",
        deps = [
            "@org_slf4j_slf4j_api",
        ],
    )
