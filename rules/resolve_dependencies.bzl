def _impl(ctx):
    artifacts_file = ctx.file.artifacts_file

    resolver = ctx.attr.resolver[DefaultInfo].files_to_run.executable.short_path

    # Determine the auto-generated aggregator name from destination
    destination = ctx.attr.destination
    auto_aggregator = destination.split("/")[-1] + ".bzl"
    aggregator_name = ctx.attr.aggregator_name if ctx.attr.aggregator_name else auto_aggregator

    # Build rename command if aggregator_name differs from auto-generated name
    rename_cmd = ""
    if ctx.attr.aggregator_name and ctx.attr.aggregator_name != auto_aggregator:
        dest_dir = "/".join(destination.split("/")[:-1]) if "/" in destination else ""
        if dest_dir:
            auto_path = dest_dir + "/" + auto_aggregator
            new_path = dest_dir + "/" + aggregator_name
        else:
            auto_path = auto_aggregator
            new_path = aggregator_name
        rename_cmd = '\n\n# Rename aggregator file\nTARGET_REPO="${{DESTINATION_DIRECTORY:-$BUILD_WORKING_DIRECTORY}}"\nif [ -f "$TARGET_REPO/{auto_path}" ]; then\n  mv "$TARGET_REPO/{auto_path}" "$TARGET_REPO/{new_path}"\n  echo "Renamed {auto_path} -> {new_path}"\nfi\n'.format(
            auto_path = auto_path,
            new_path = new_path,
        )

    cmd_parts = [
        "#!/bin/bash\n\nset -euo pipefail\n\n",
        "{resolver}",
        " {artifacts_file}",
        " {target_repo}",
        " --repository-urls={repository_urls}",
        " --import-external-rule-path=\"{import_external_rule_path}\"",
        " --import-external-macro-name=\"{import_external_macro_name}\"",
        " --remote-resolver-url=\"{remote_resolver_url}\"",
        " --destination=\"{destination}\"",
        " $@",
        "{rename_cmd}",
    ]

    cmd = "".join(cmd_parts).format(
        resolver = resolver,
        artifacts_file = artifacts_file.short_path,
        target_repo = "${DESTINATION_DIRECTORY:-$BUILD_WORKING_DIRECTORY}",
        repository_urls = ",".join(ctx.attr.repository_urls),
        import_external_rule_path = ctx.attr.import_external_rule_path,
        import_external_macro_name = ctx.attr.import_external_macro_name,
        remote_resolver_url = ctx.attr.remote_resolver_url,
        destination = ctx.attr.destination,
        rename_cmd = rename_cmd,
    )

    additional_runfiles = []

    exec = ctx.actions.declare_file(ctx.label.name + "_script.sh")

    ctx.actions.write(
        output = exec,
        content = cmd,
        is_executable = True,
    )

    # compute runfiles
    runfiles = ctx.runfiles(
        files = [exec, artifacts_file] + additional_runfiles,
    ).merge(ctx.attr.resolver[DefaultInfo].default_runfiles)

    return [DefaultInfo(executable = exec, runfiles = runfiles)]

resolve_dependencies = rule(
    implementation = _impl,
    attrs = {
        "artifacts_file": attr.label(allow_single_file = True, mandatory = True),
        "repository_urls": attr.string_list(
            default = [
                "https://repo1.maven.org/maven2",
            ],
        ),
        "import_external_rule_path": attr.string(mandatory = True),
        "import_external_macro_name": attr.string(mandatory = True),
        "remote_resolver_url": attr.string(mandatory = False),
        "destination": attr.string(
            default = "third_party",
            doc = "Destination path for generated .bzl files relative to workspace root",
        ),
        "aggregator_name": attr.string(
            mandatory = False,
            doc = "Name for the aggregator .bzl file. If not set, defaults to <destination_folder>.bzl",
        ),
        "resolver": attr.label(
            providers = [DefaultInfo],
            default = "@rules_maven_third_party//private/synchronizer/cli/src/main/scala/com/wix/build/sync/cli",
        ),
    },
    executable = True,
)
