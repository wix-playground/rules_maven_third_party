load(":import_external.bzl", import_external = "import_external", maven_archive = "maven_archive")

def dependencies():
    import_external(
        name = "com_fasterxml_jackson_datatype_jackson_datatype_jdk8",
        artifact = "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.4",
        artifact_sha256 = "00852350fc1503344723b590f1afe9593ab732fb5b035659b503b49bbea5c9b2",
        srcjar_sha256 = "480de9700a341cfb58adb26a87372c934c3b3f3f1dba5ec319796317c7524e64",
        deps = [
            "@com_fasterxml_jackson_core_jackson_core",
            "@com_fasterxml_jackson_core_jackson_databind",
        ],
    )


    import_external(
        name = "com_fasterxml_jackson_datatype_jackson_datatype_joda",
        artifact = "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.12.4",
        artifact_sha256 = "0de93d725472df2027c3e869301a3035892e607d94423c589c96964305d51051",
        srcjar_sha256 = "999d17249f6d4491656e123b4ef49e326a57b1ec771c0b67834cc4b4ca692b45",
        deps = [
            "@com_fasterxml_jackson_core_jackson_annotations",
            "@com_fasterxml_jackson_core_jackson_core",
            "@com_fasterxml_jackson_core_jackson_databind",
            "@joda_time_joda_time",
        ],
    )


    import_external(
        name = "com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        artifact = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.4",
        artifact_sha256 = "af5a384d020e43f91f56d083f170d67aaf5aead71fa8fa1ad80a425b13ba13e4",
        srcjar_sha256 = "5d080278c2b98374a419c6b78586072de0c888f82618106a1e27ff20249d4b20",
        deps = [
            "@com_fasterxml_jackson_core_jackson_annotations",
            "@com_fasterxml_jackson_core_jackson_core",
            "@com_fasterxml_jackson_core_jackson_databind",
        ],
    )
