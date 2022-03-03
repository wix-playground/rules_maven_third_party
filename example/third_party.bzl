load("//:third_party/org_hamcrest.bzl", org_hamcrest_deps = "dependencies")
load("//:third_party/junit.bzl", junit_deps = "dependencies")

def managed_third_party_dependencies():
    junit_deps()

    org_hamcrest_deps()
