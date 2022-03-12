load("@rules_maven_third_party//rules:maven_specs.bzl", "wix")

BUILD_TPL = """
load("@rules_maven_third_party//rules:resolve_dependencies.bzl", "resolve_dependencies")

filegroup(
    name = "artifacts",
    srcs = ["artifacts_list"],
    visibility = ["//visibility:public"]
)
"""

MANAGED_RESOLVE_TPL = """
resolve_dependencies(
    name = "resolve",
    artifacts_file = ":artifacts",
    repository_urls = {repository_urls},
    import_external_rule_path = "{import_external_rule_path}",
    import_external_macro_name = "{import_external_macro_name}",
    remote_resolver_url = "{remote_resolver_url}",
)
"""

# parsing is inlined implementation from rules_jvm_external
#
# Parsing tools
#
def _parse_exclusion_spec_list(exclusion_specs):
    """
    Given a string (g:a), returns an exclusion map
    """
    exclusions = []
    for exclusion_spec in exclusion_specs:
        if type(exclusion_spec) == "string":
            pieces = exclusion_spec.split(":")
            if len(pieces) == 2:
                exclusion_spec = {"group": pieces[0], "artifact": pieces[1]}
            else:
                fail(("Invalid exclusion: %s. Exclusions are specified as " +
                      "group-id:artifact-id, without the version, packaging or " +
                      "classifier.") % exclusion_spec)
        exclusions.append(exclusion_spec)
    return exclusions

def _parse_maven_coordinate_string(mvn_coord):
    """
    Given a string containing a standard Maven coordinate (g:a:[p:[c:]]v), returns a maven artifact map (see above).
    """
    pieces = mvn_coord.split(":")
    group = pieces[0]
    artifact = pieces[1]

    if len(pieces) == 3:
        version = pieces[2]
        return {"group": group, "artifact": artifact, "version": version}
    elif len(pieces) == 4:
        packaging = pieces[2]
        version = pieces[3]
        return {"group": group, "artifact": artifact, "packaging": packaging, "version": version}
    elif len(pieces) == 5:
        packaging = pieces[2]
        classifier = pieces[3]
        version = pieces[4]
        return {"group": group, "artifact": artifact, "packaging": packaging, "classifier": classifier, "version": version}
    else:
        fail("Could not parse maven coordinate", attr = mvn_coord)

def _parse_artifact_spec_list(artifact_specs):
    """
    Given a list containing either strings or artifact maps (see above), returns a list containing artifact maps.
    """
    artifacts = []
    for artifact in artifact_specs:
        if type(artifact) == "string":
            artifacts.append(_parse_maven_coordinate_string(artifact))
        else:
            artifacts.append(artifact)
    return artifacts

def _artifact_to_json(artifact_spec):
    artifact = dict(
        artifact_spec.items(),
        exclusions = _parse_exclusion_spec_list(artifact_spec.get("exclusions") or []),
    )

    return artifact

def _artifacts_to_json(artifact_specs):
    return json.encode_indent(
        [_artifact_to_json(artifact) for artifact in _parse_artifact_spec_list(artifact_specs)],
        indent = "  ",
    )

def _impl(repository_ctx):
    repository_ctx.file("artifacts_list", repository_ctx.attr.artifacts)

    repository_ctx.file(
        "BUILD.bazel",
        BUILD_TPL + MANAGED_RESOLVE_TPL.format(
            repository_urls = str(repository_ctx.attr.repository_urls),
            import_external_rule_path = repository_ctx.attr.import_external_rule_path,
            import_external_macro_name = repository_ctx.attr.import_external_macro_name,
            remote_resolver_url = repository_ctx.attr.remote_resolver_url,
        ),
    )

_managed_third_party = repository_rule(
    implementation = _impl,
    attrs = {
        "artifacts": attr.string(
            doc = "list of artifact objects as json",
        ),
        "repository_urls": attr.string_list(
            doc = "list of maven repository servers",
        ),
        "import_external_rule_path": attr.string(
            doc = "bzl file to load rule for generated external definitions",
        ),
        "import_external_macro_name": attr.string(
            doc = "name to be loaded from bzl file to load rule for generated external definitions",
        ),
        "remote_resolver_url": attr.string(
            mandatory = False,
            doc = "remote resolver url if supported",
        ),
    },
)

def managed_third_party(
        artifacts,
        name = "managed_third_party",
        remote_resolver_url = None,
        **kwargs):
    artifacts_json = _artifacts_to_json(artifacts)

    _managed_third_party(
        name = name,
        remote_resolver_url = remote_resolver_url,
        artifacts = artifacts_json,
        **kwargs
    )
