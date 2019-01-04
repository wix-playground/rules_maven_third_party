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
        PATH = "$BAZEL_HOME/bin:$JAVA_HOME/bin:$PATH"

        MANAGED_DEPS_REPO_NAME = "core-server-build-tools"
        MANAGED_DEPS_REPO_URL = "git@github.com:wix-private/core-server-build-tools.git"

        TARGET_REPO_NAME = "target-repo"
        TARGET_REPO_URL = "${env.TARGET_REPO_URL}"

        FW_LEAF_ARTIFACT = "${env.FW_LEAF_ARTIFACT}"
        BRANCH_NAME = "fw-ga-sync-${env.BUILD_ID}"
    }
    stages {
        stage('build-fw-deps-to-single-repo-sync') {
            steps {
                script{
                    sh  """|#!/bin/bash
                           |bazel ${env.BAZEL_STARTUP_OPTS} \\
                           |build \\
                           |      ${env.BAZEL_FLAGS} \\
                           |      //dependency-synchronizer/bazel-fw-deps-synchronizer-cli/src/main/scala/com/wix/build/sync/fw:fw_to_single_repo_sync_cli_deploy.jar
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
        stage('sync-fw-ga-to-single-repo') {
            steps {
                script {
                    sh """|stdbuf -i0 -o0 -e0 \\
                          |   java -Xmx12G \\
                          |   -jar bazel-bin/dependency-synchronizer/bazel-fw-deps-synchronizer-cli/src/main/scala/com/wix/build/sync/fw/fw_to_single_repo_sync_cli_deploy.jar --target_repo ${env.TARGET_REPO_NAME} --managed_deps_repo ${env.MANAGED_DEPS_REPO_NAME} --fw-leaf-artifact ${env.FW_LEAF_ARTIFACT}""".stripMargin()
                }
            }
        }

        stage('push-to-git') {
            steps {
                dir("${env.TARGET_REPO_NAME}"){
                    sh """|git checkout -b ${env.BRANCH_NAME}
                          |git add .
                          |git commit --allow-empty -m "GAed FW sync by ${env.BUILD_URL} #automerge"
                          |git push origin ${env.BRANCH_NAME}
                          |""".stripMargin()
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts "bazel-bin/dependency-synchronizer/bazel-fw-deps-synchronizer-cli/src/main/scala/com/wix/build/sync/fw/fw_to_single_repo_sync_cli_deploy.jar"
        }
    }
}