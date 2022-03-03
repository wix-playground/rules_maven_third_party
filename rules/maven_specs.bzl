def _maven_artifact(
        group,
        artifact,
        version,
        ownership_tag = None,
        packaging = None,
        classifier = None,
        exclusions = None,
        neverlink = None,
        testonly = None,
        tags = None,
        flatten_transitive_deps = None,
        aliases = None):
    """Defines maven artifact by coordinates.
    Args:
        group: The Maven artifact coordinate group name (ex: "com.google.guava").
        artifact: The Maven artifact coordinate artifact name (ex: "guava").
        version: The Maven artifact coordinate version name (ex: "1.20.1").
        ownership_tag: 3rd party dependency owner responsible for its maintenance.
        packaging:The Maven artifact coordinate packaging name (ex: "jar").
        classifier: The Maven artifact coordinate classifier name (ex: "jdk11").
        exclusions: Artifact dependencies to be excluded from resolution closure.
        neverlink: neverlink value to set,
        testonly: testonly value to set.
        tags: Target tags.
        flatten_transitive_deps: Define all transitive deps as direct deps.
        aliases: aliases that will point to this dep.
    """
    maven_artifact = {}
    maven_artifact["group"] = group
    maven_artifact["artifact"] = artifact
    maven_artifact["version"] = version
    maven_artifact["aliases"] = aliases
    maven_artifact["tags"] = tags
    maven_artifact["flatten_transitive_deps"] = flatten_transitive_deps

    if packaging != None:
        maven_artifact["packaging"] = packaging
    if classifier != None:
        maven_artifact["classifier"] = classifier
    if exclusions != None:
        maven_artifact["exclusions"] = exclusions
    if neverlink != None:
        maven_artifact["neverlink"] = neverlink
    if testonly != None:
        maven_artifact["testonly"] = testonly
    if ownership_tag != None:
        maven_artifact["ownership_tag"] = ownership_tag

    return maven_artifact

def _maven_exclusion(group, artifact):
    return {
        "group": group,
        "artifact": artifact,
    }

wix = struct(
    artifact = _maven_artifact,
    exclusion = _maven_exclusion,
)
