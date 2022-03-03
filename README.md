# Rules Maven Third Party

Maven artifact resolver for Bazel JVM projects

## Usage 
See [example](examples/) for a complete workspace.

Running `bazel run @managed_third_party//:resolve -- --resolve-locally` generates pin 
under `third_party` directory. Default location can be changed with `--destination` command line option.

Pin files can be loaded with:
```starlark
load("//:third_party.bzl", "managed_third_party_dependencies")

managed_third_party_dependencies()
```

`@managed_third_party` repository is configured with:
```starlark
managed_third_party(
    artifacts = MANAGED_DEPS + TOOL_DEPS,
    import_external_macro_name = "import_external",
    import_external_rule_path = "@rules_maven_third_party//:import_external.bzl",
    repository_urls = REPOSITORY_URLS,
)
```

`artifacts` is a list of high level artifact definitions. Example of such list:
```starlark
load("@rules_maven_third_party//rules:maven_specs.bzl", "wix")

MANAGED_DEPS = [
    wix.artifact(group = "junit", artifact = "junit", version = "4.13.2"),
]
```
`import_external_macro_name` and `import_external_rule_path` configure to load statement in generated pin file: 
```starlark
load("@rules_maven_third_party//:import_external.bzl", import_external = "import_external")

def dependencies():
    import_external(
        name = "junit_junit",
        artifact = "junit:junit:4.13.2",
        artifact_sha256 = "8e495b634469d64fb8acfa3495a065cbacc8a0fff55ce1e31007be4c16dc57d3",
        srcjar_sha256 = "34181df6482d40ea4c046b063cb53c7ffae94bdf1b1d62695bdf3adf9dea7e3a",
        deps = [
            "@org_hamcrest_hamcrest_core",
        ],
    )
```
`repository_urls` allows to specify list of maven repositories to look for artifacts

# wix.artifact attributes:
`group`: The Maven artifact coordinate group name (ex: "com.google.guava").
`artifact`: The Maven artifact coordinate artifact name (ex: "guava").
`version`: The Maven artifact coordinate version name (ex: "1.20.1").
`ownership_tag`: 3rd party dependency owner responsible for its maintenance. *not implemented*
`packaging`: The Maven artifact coordinate packaging name (ex: "jar").
`classifier`: The Maven artifact coordinate classifier name (ex: "jdk11").
`exclusions`: Artifact dependencies to be excluded from resolution closure.
`neverlink`: neverlink value to set,
`testonly`: testonly value to set. *not implemented*
`tags`: Target tags.
`flatten_transitive_deps`: Define all transitive deps as direct deps.
`aliases`: import_external_aliases that will point to this dep.

