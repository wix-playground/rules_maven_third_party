load(":import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "com_google_guava_guava",
        artifact = "com.google.guava:guava:20.0",
        artifact_sha256 = "36a666e3b71ae7f0f0dca23654b67e086e6c93d192f60ba5dfd5519db6c288c8",
        srcjar_sha256 = "994be5933199a98e98bd09584da2bb69ed722275f6bed61d83459af88ace5cbd",
    )
