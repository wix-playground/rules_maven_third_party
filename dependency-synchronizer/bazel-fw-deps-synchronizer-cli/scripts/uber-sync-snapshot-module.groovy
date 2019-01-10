// must have sandbox off so you must paste this code directly into pipeline job (don't choose SCM)
import jenkins.*

node {
    git "git@github.com:wix-private/bazel-tooling.git"
    brsConfig = readJSON file: "wix-bazel-repositories/bazel-repositories-server/src/main/resources/bazel-repositories-server-config.json.erb"
//    echo brsConfig
        allRepos = brsConfig['repositoriesUrls']["GCB Triggered"]
    echo allRepos.toString()

    // TODO: validate MODULE_COORDINATES is correct structure


    if (!allRepos.any{(it == "${env.ORIGINATING_REPO_URL}")}) {
        error("ORIGINATING_REPO_URL '${env.ORIGINATING_REPO_URL}' is missing from wix bazel repos list!" )
    }
    repos = allRepos.findAll{it != "git@github.com:wix-private/core-server-build-tools.git" && it != "${env.ORIGINATING_REPO_URL}"}

    def jobs = [:]
    repos.each {
        def parameters = [
                string(name: 'MODULE_COORDINATES', value: "${env.MODULE_COORDINATES}"),
                string(name: 'TARGET_REPO_URL', value: it),
        ]
        jobs[it] = {
//            echo "triggering sync-snapshot-module-to-single-repo with paramerters: wait: true, propagate: false, parameters: ${parameters}"
            build job: "sync-snapshot-module-to-single-repo", wait: true, propagate: false, parameters: parameters
        }
    }
    parallel jobs
}
