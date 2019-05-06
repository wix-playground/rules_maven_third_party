pipeline {
    agent any
    options {
        timeout(time: 90, unit: 'MINUTES')
        timestamps()
        ansiColor('xterm')
    }
    tools{
        jdk 'jdk8'
    }
    environment {
        GOOGLE_APPLICATION_CREDENTIALS = credentials("rbe_credentials")
        bazel_log_file = "bazel-build.log"
        BAZEL_STARTUP_OPTS = '''|--bazelrc=.bazelrc.remote \\
                                |'''.stripMargin()
        BAZEL_HOME = tool name: 'bazel', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
        BUILDOZER_HOME = tool name: 'buildozer', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
        BUILDIFIER_HOME = tool name: 'buildifier', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
        PATH = "$BAZEL_HOME/bin:$BUILDOZER_HOME/bin:$BUILDIFIER_HOME/bin:$JAVA_HOME/bin:$PATH"

        MANAGED_DEPS_REPO_NAME = "core-server-build-tools"
        MANAGED_DEPS_REPO_URL = "git@github.com:wix-private/core-server-build-tools.git"

        TARGET_REPO_NAME = "target-repo"
        TARGET_REPO_URL = "${env.TARGET_REPO_URL}"

        MODULE_COORDINATES = "${env.MODULE_COORDINATES}"
        BRANCH_NAME = "snapshot-module-sync-${env.BUILD_ID}"
    }
    stages {
        stage('prepare-env') {
            steps {
                script {
                    currentBuild.description = """${env.TARGET_REPO_URL}<br/>${
                        env.MODULE_COORDINATES
                    }"""
                    deleteDir()
                    copyArtifacts flatten: true, projectName: 'build-cli', selector: lastSuccessful()
                    copyArtifacts flatten: true, projectName: '/depfixer-build', selector: lastSuccessful()
                }
            }
        }
        stage('checkout-managed-deps-repo') {
            steps {
                echo "checkout of: ${env.MANAGED_DEPS_REPO_NAME}"
                dir("${env.MANAGED_DEPS_REPO_NAME}") {
                    git url: env.MANAGED_DEPS_REPO_URL, branch: "master"
                }
            }
        }
        stage('checkout-target-repo') {
            steps {
                echo "checkout of: ${env.TARGET_REPO_URL}"
                dir(env.TARGET_REPO_NAME) {
                    git url: env.TARGET_REPO_URL, branch: "master"
                }
            }
        }
        stage('sync-snapshot-module-to-single-repo') {
            steps {
                script {
                    sh """|stdbuf -i0 -o0 -e0 \\
                          |   java -Xmx12G \\
                          |   -jar sync_cli_deploy.jar ${env.TARGET_REPO_NAME} ${env.MODULE_COORDINATES} --override_managed_deps_repo ${env.MANAGED_DEPS_REPO_NAME}""".stripMargin()
                }
            }
        }
        stage('fix_strict_deps') {
            when{
                expression { return params.FIX_STRICT_DEPS}
            }
            steps {
                dir("${env.TARGET_REPO_NAME}") {
                    script {
                        BAZEL_FLAGS_LIST = ["-k",
                                            "--experimental_remap_main_repo=true",
                                            "--config=remote",
                                            "--config=rbe_based",
                                            "--config=results",
                                            "--project_id=gcb-with-custom-workers",
                                            "--build_tag_filters=-deployable",
                                            "--remote_instance_name=projects/gcb-with-custom-workers/instances/default_instance"
                        ]
                        bazelrc = readFile("tools/bazelrc/.bazelrc.managed.dev.env")
                        if (bazelrc.contains("--extra_toolchains=@core_server_build_tools//toolchains:wix_plus_one_global_toolchain"))
                            run_depfixer(BAZEL_FLAGS_LIST)
                        else
                            build_and_fix(BAZEL_FLAGS_LIST)
                    }
                }
            }
        }
        stage('push-to-git') {
            steps {
                dir("${env.TARGET_REPO_NAME}") {
                    script {
                        foundDiff = sh script: "git diff --exit-code", returnStatus: true
                        if (foundDiff > 0) {
                            sh """|git checkout -b ${env.BRANCH_NAME}
                                  |git add .
                                  |git commit --allow-empty -m "a new version of '${env.MODULE_COORDINATES}' was synced by ${env.BUILD_URL} #automerge #gcb_no_trigger_other_repos"
                                  |git push origin ${env.BRANCH_NAME}
                                  |""".stripMargin()
                        } else {
                            currentBuild.description = """<div class="warning">no-change</div>${env.TARGET_REPO_URL}<br/>${params.COMMIT_MESSAGE_PREFIX}"""
                        }
                    }
                }
            }
        }
    }
    post {
        failure{
            script {
                msg = "*:thumbsdown: Sync SNAPSHOT Jenkins job Failed :thumbsdown:*\n *Repo*:`${env.TARGET_REPO_URL}`\n*Artifacts*:`${env.MODULE_COORDINATES}`\nsee more here ${env.BUILD_URL} "
                slackSend channel: "#automerge-status", color: "warning", message: msg
            }
        }
    }
}

def build_and_fix(BAZEL_FLAGS_LIST) {
    status = sh(
            script: """|#!/bin/bash
                       |# tee would output the stdout to file but will swallow the exit code
                       |bazel --output_base=${env.WORKSPACE}/output_base --bazelrc=.bazelrc.remote build ${BAZEL_FLAGS_LIST.join(" ")}  //... 2>&1 | tee bazel-build.log
                       |# retrieve the exit code
                       |exit \${PIPESTATUS[0]}
                       |""".stripMargin(),
            returnStatus: true)
    build_log = readFile "bazel-build.log"
    if (build_log.contains("buildozer") || build_log.contains("[strict]")) {
        if (build_log.contains("Unknown label of file")){
            slackSend "Found 'Unknown label...' warning in ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|link>)"
        }
        echo "found strict deps issues"
        sh "python ../core-server-build-tools/scripts/fix_transitive.py"
        buildozerStatusCode = sh script: "buildozer -f bazel-buildozer-commands.txt", returnStatus: true
        if (buildozerStatusCode == 0) { // buildozer returns 3 when no action was needed
            build_and_fix(BAZEL_FLAGS_LIST)
        } else {
            echo "buildozer exited with code ${buildozerStatusCode}"
            echo "[WARN] produced buildozer commands were not required!"
            currentBuild.result = 'UNSTABLE'
        }

    } else if (status == 0) {
        echo "No buildozer warnings were found"
        bazelrc = readFile(".bazelrc")
        if (bazelrc.contains("strict_java_deps=warn")) {
            writeFile file: ".bazelrc", text: bazelrc.replace("strict_java_deps=warn", "strict_java_deps=error")
        }
    } else {
        echo "[WARN] No strict deps warnings found but build failed"
        currentBuild.result = 'UNSTABLE'
    }
}

def run_depfixer(BAZEL_FLAGS_LIST) {
    bazelrc_remote = readFile(".bazelrc.remote")
    bazelrc_remote = bazelrc_remote.replace("import %workspace%/.bazelrc","#import %workspace%/.bazelrc")
    sh "mv .bazelrc .user.input.bazelrc"
    new_bazelrc = """|# ========================
                     |# temp generated bazelrc
                     |# =======================
                     |import %workspace%/.user.input.bazelrc
                     |import %workspace%/.bazelrc.remote
                     |
                     |""".stripMargin() + BAZEL_FLAGS_LIST.collect { "build " + it }.join("\n")
    echo new_bazelrc
    writeFile file: ".bazelrc", text: new_bazelrc
    writeFile file: ".bazelrc.remote", text: bazelrc_remote
    status = sh(
            script: "java -jar ../depfixer.jar -targets //...",
            returnStatus: true)
    sh "rm .user.input.bazelrc"
    sh "git checkout .bazelrc"
    some_other_status = sh( script: "git checkout .bazelrc.remote", returnStatus: true)
    if (status != 0) {
        currentBuild.result = 'FAILURE'
    }
}
