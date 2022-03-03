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

def _parse_repository_spec_list(repository_specs):
    """
    Given a list containing either strings or repository maps (see above), returns a list containing repository maps.
    """
    repos = []
    for repo in repository_specs:
        if type(repo) == "string":
            repos.append({"repo_url": repo})
        else:
            repos.append(repo)
    return repos

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

def _artifact_spec_to_json(artifact_spec):
    """
    Given an artifact spec, returns the json serialization of the object.
    """
    maybe_exclusion_specs_jsons = []
    for spec in _parse_exclusion_spec_list(artifact_spec.get("exclusions") or []):
        maybe_exclusion_specs_jsons.append(_exclusion_spec_to_json(spec))
    exclusion_specs_json = (("[" + ", ".join(maybe_exclusion_specs_jsons) + "]") if len(maybe_exclusion_specs_jsons) > 0 else None)

    required = "{ \"group\": \"" + artifact_spec["group"] + \
               "\", \"artifact\": \"" + artifact_spec["artifact"] + \
               "\", \"version\": \"" + artifact_spec["version"] + "\""

    with_packaging = required + ((", \"packaging\": \"" + artifact_spec["packaging"] + "\"") if artifact_spec.get("packaging") != None else "")
    with_classifier = with_packaging + ((", \"classifier\": \"" + artifact_spec["classifier"] + "\"") if artifact_spec.get("classifier") != None else "")
    with_exclusions = with_classifier + ((", \"exclusions\": " + exclusion_specs_json) if artifact_spec.get("exclusions") != None else "")
    with_neverlink = with_exclusions + ((", \"neverlink\": " + str(artifact_spec.get("neverlink")).lower()) if artifact_spec.get("neverlink") != None else "")
    with_testonly = with_neverlink + ((", \"testonly\": " + str(artifact_spec.get("testonly")).lower()) if artifact_spec.get("testonly") != None else "")
    with_flatten = with_testonly + ((", \"flattenTransitiveDeps\": " + str(artifact_spec.get("flatten_transitive_deps")).lower()) if artifact_spec.get("flatten_transitive_deps") != None else "")
    with_aliases = with_flatten + ((", \"aliases\": " + str(artifact_spec.get("aliases"))) if artifact_spec.get("aliases") != None else "")
    with_tags = with_aliases + ((", \"tags\": " + str(artifact_spec.get("tags"))) if artifact_spec.get("tags") != None else "")

    return with_tags + " }"

def _exclusion_spec_to_json(exclusion_spec):
    """
    Given an artifact exclusion spec, returns the json serialization of the object.
    """
    return "{ \"group\": \"" + exclusion_spec["group"] + "\", \"artifact\": \"" + exclusion_spec["artifact"] + "\" }"

def _impl(repository_ctx):
    repository_ctx.file("artifacts_list", "\n".join(repository_ctx.attr.artifacts))

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
        "artifacts": attr.string_list(),  # list of artifact objects, each as json
        "repository_urls": attr.string_list(),  # list of maven repository servers
        "import_external_rule_path": attr.string(),  # bzl file to load rule for generated external definitions
        "import_external_macro_name": attr.string(),  # name to be loaded from bzl file to load rule for generated external definitions
        "remote_resolver_url": attr.string(mandatory = False),  # remote resolver url if supported
        "_artifacts_indicator": attr.label(default = "@core_server_build_tools//third_party/maven:managed_artifacts.bzl"),
    },
)

def managed_third_party(
        artifacts,
        name = "managed_third_party",
        remote_resolver_url = None,
        **kwargs):
    artifacts_json_strings = []
    for artifact in _parse_artifact_spec_list(artifacts):
        artifacts_json_strings.append(_artifact_spec_to_json(artifact))

    _managed_third_party(
        name = name,
        remote_resolver_url = remote_resolver_url,
        artifacts = artifacts_json_strings,
        **kwargs
    )
