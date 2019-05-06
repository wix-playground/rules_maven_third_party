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
        BAZEL_FLAGS = '''--config=remote \\
                        |--config=results \\
                        |--config=rbe_based \\
                        |--project_id=gcb-with-custom-workers \\
                        |--remote_instance_name=projects/gcb-with-custom-workers/instances/default_instance'''.stripMargin()
        BAZEL_HOME = tool name: 'bazel', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
        PATH = "$BAZEL_HOME/bin:$JAVA_HOME/bin:$PATH"
    }
    stages {
        stage('build-migrator') {
            steps {
                script{
                    sh  """|#!/bin/bash
                           |rm -f sync_cli_deploy.jar
                           |bazel ${env.BAZEL_STARTUP_OPTS} \\
                           |build \\
                           |      ${env.BAZEL_FLAGS} \\
                           |      //dependency-synchronizer/bazel-deps-synchronizer-cli/src/main/scala/com/wix/build/sync:sync_cli_deploy.jar
                           |""".stripMargin()

                    sh "cp bazel-bin/dependency-synchronizer/bazel-deps-synchronizer-cli/src/main/scala/com/wix/build/sync/sync_cli_deploy.jar sync_cli_deploy.jar"
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts "sync_cli_deploy.jar"
        }
    }
}