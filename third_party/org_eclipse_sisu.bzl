load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "org_eclipse_sisu_org_eclipse_sisu_inject",
        artifact = "org.eclipse.sisu:org.eclipse.sisu.inject:0.3.5",
        artifact_sha256 = "c5994010bcdce1d2bd603a4d50c47191ddbd7875d1157b23aaa26d33c82fda13",
        srcjar_sha256 = "d8877eabe79aafaf64fb10e94fad4b53ab89d587d48a935af276e45ea13427bc",
    )


    import_external(
        name = "org_eclipse_sisu_org_eclipse_sisu_plexus",
        artifact = "org.eclipse.sisu:org.eclipse.sisu.plexus:0.0.0.M5",
        artifact_sha256 = "ea8e63f336673f6a6e6f0b837e69578c151417c1113a578568f5e99a5a165f29",
        srcjar_sha256 = "bb4efd199dd8ef01b4923c1c34c554ea893952208fe77489a1568810ad448745",
        deps = [
            "@com_google_guava_guava",
            "@javax_enterprise_cdi_api",
            "@org_codehaus_plexus_plexus_classworlds",
            "@org_codehaus_plexus_plexus_component_annotations",
            "@org_codehaus_plexus_plexus_utils",
            "@org_eclipse_sisu_org_eclipse_sisu_inject",
            "@org_sonatype_sisu_sisu_guice_no_aop",
        ],
    )
