def _impl(ctx):
    artifacts_file = ctx.file.artifacts_file

    resolver = ctx.attr._resolver[DefaultInfo].files_to_run.executable.short_path

    cmd_parts = [
        "#!/bin/bash\n\nset -euo pipefail\n\n",
        "{resolver}",
        " {artifacts_file}",
        " {target_repo}",
        " --repository-urls={repository_urls}",
        " --import-external-rule-path=\"{import_external_rule_path}\"",
        " --import-external-macro-name=\"{import_external_macro_name}\"",
        " --remote-resolver-url=\"{remote_resolver_url}\"",
        " $@\n",
    ]

    cmd = "".join(cmd_parts).format(
        resolver = resolver,
        artifacts_file = artifacts_file.short_path,
        target_repo = "${DESTINATION_DIRECTORY:-$BUILD_WORKING_DIRECTORY}",
        repository_urls = ",".join(ctx.attr.repository_urls),
        import_external_rule_path = ctx.attr.import_external_rule_path,
        import_external_macro_name = ctx.attr.import_external_macro_name,
        remote_resolver_url = ctx.attr.remote_resolver_url,
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
    ).merge(ctx.attr._resolver[DefaultInfo].default_runfiles)

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
        "_resolver": attr.label(
            providers = [DefaultInfo],
            default = "@rules_maven_third_party//private/synchronizer/cli/src/main/scala/com/wix/build/sync/cli",
        ),
    },
    executable = True,
)
