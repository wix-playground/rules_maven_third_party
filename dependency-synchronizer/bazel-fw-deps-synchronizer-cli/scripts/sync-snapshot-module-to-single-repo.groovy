pipeline {
    agent any
    options {
        timeout(time: 15, unit: 'MINUTES')
        timestamps()
        ansiColor('xterm')
    }
    tools{
        jdk 'jdk8'
    }
    environment {
        GOOGLE_APPLICATION_CREDENTIALS = credentials("rbe_credentials")
        BAZEL_STARTUP_OPTS = '''|--bazelrc=.bazelrc.remote \\
                                |'''.stripMargin()
        BAZEL_FLAGS = '''|-k \\
                         |--config=remote \\
                         |--config=results \\
                         |--config=rbe_based \\
                         |--project_id=gcb-with-custom-workers \\
                         |--remote_instance_name=projects/gcb-with-custom-workers/instances/default_instance'''.stripMargin()
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
        stage('build-snapshot-module-to-single-repo-sync') {
            steps {
                script{
                    currentBuild.description = """${env.TARGET_REPO_URL}<br/>${env.MODULE_COORDINATES}"""
                    sh  """|#!/bin/bash
                           |bazel ${env.BAZEL_STARTUP_OPTS} \\
                           |build \\
                           |      ${env.BAZEL_FLAGS} \\
                           |      //dependency-synchronizer/bazel-fw-deps-synchronizer-cli/src/main/scala/com/wix/build/sync/snapshot:snapshot_to_single_repo_sync_cli_deploy.jar
                           |""".stripMargin()

                }
            }
        }
        stage('checkout-managed-deps-repo') {
            steps {
                echo "checkout of: ${env.MANAGED_DEPS_REPO_NAME}"
                dir("${env.MANAGED_DEPS_REPO_NAME}") {
                    checkout([$class: 'GitSCM', branches: [[name: 'master' ]],
                              userRemoteConfigs: [[url: "${env.MANAGED_DEPS_REPO_URL}"]]])
                }
            }
        }
        stage('checkout-target-repo') {
            steps {
                echo "checkout of: ${env.TARGET_REPO_URL}"
                dir("${env.TARGET_REPO_NAME}") {
                    checkout([$class: 'GitSCM', branches: [[name: 'master' ]],
                              userRemoteConfigs: [[url: "${env.TARGET_REPO_URL}"]]])
                }
            }
        }
        stage('sync-snapshot-module-to-single-repo') {
            steps {
                script {
                    sh """|stdbuf -i0 -o0 -e0 \\
                          |   java -Xmx12G \\
                          |   -jar bazel-bin/dependency-synchronizer/bazel-fw-deps-synchronizer-cli/src/main/scala/com/wix/build/sync/snapshot/snapshot_to_single_repo_sync_cli_deploy.jar --target_repo ${env.TARGET_REPO_NAME} --managed_deps_repo ${env.MANAGED_DEPS_REPO_NAME} --snapshot_modules ${env.MODULE_COORDINATES}""".stripMargin()
                }
            }
        }
        stage('fix_strict_deps') {
            when{
                when { expression { return params.FIX_STRICT_DEPS} }
            }
            steps {
                dir("${env.TARGET_REPO_NAME}") {
                    script {
                        build_and_fix(env.ADDITIONAL_FLAGS_BAZEL_SIXTEEN_UP_LOCAL)
                    }
                }
            }
        }
        stage('push-to-git') {
            steps {
                dir("${env.TARGET_REPO_NAME}"){
                    sh """|git checkout -b ${env.BRANCH_NAME}
                          |git add .
                          |git commit --allow-empty -m "a new version of '${env.MODULE_COORDINATES}' was synced by ${env.BUILD_URL} #automerge #gcb_no_trigger_other_repos"
                          |git push origin ${env.BRANCH_NAME}
                          |""".stripMargin()
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts "bazel-bin/dependency-synchronizer/bazel-fw-deps-synchronizer-cli/src/main/scala/com/wix/build/sync/snapshot/snapshot_to_single_repo_sync_cli_deploy.jar"
        }
    }
}

def build_and_fix(ADDITIONAL_FLAGS_BAZEL_SIXTEEN_UP_LOCAL) {
    BAZEL_FLAGS = """|-k \\
                     |--experimental_remap_main_repo=true \\
                     |--config=remote \\
                     |--config=rbe_based \\
                     |--config=results \\
                     |--project_id=gcb-with-custom-workers \\
                     |--remote_instance_name=projects/gcb-with-custom-workers/instances/default_instance""".stripMargin()
    status = sh(
            script: """|#!/bin/bash
                       |# tee would output the stdout to file but will swallow the exit code
                       |bazel --bazelrc=.bazelrc.remote build ${BAZEL_FLAGS}  //... 2>&1 | tee bazel-build.log
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
            build_and_fix(ADDITIONAL_FLAGS_BAZEL_SIXTEEN_UP_LOCAL)
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