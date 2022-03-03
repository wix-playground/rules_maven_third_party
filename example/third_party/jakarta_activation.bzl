load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "jakarta_activation_jakarta_activation_api",
        artifact = "jakarta.activation:jakarta.activation-api:1.2.2",
        artifact_sha256 = "a187a939103aef5849a7af84bd7e27be2d120c410af291437375ffe061f4f09d",
        srcjar_sha256 = "d796357781c245863f310599daf09fa95e8deacb61d150ab73b9db9cfff0dbab",
    )
