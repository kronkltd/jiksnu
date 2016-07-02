#!groovy

def err
def repo = 'repo.jiksnu.org/'

// Set build properties
properties([[$class: 'GithubProjectProperty', displayName: 'Jiksnu', projectUrlStr: 'https://github.com/duck1123/jiksnu/'],
            [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false]])

node {
    wrap([$class: 'AnsiColorBuildWrapper']) {
        stage 'Prepare Environment'

        // Set the path
        env.PATH = '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'

        // Set current git commit
        checkout scm
        sh 'git submodule sync'
        sh "git rev-parse HEAD | tr -d '\n' > git-commit"
        // sh 'git rev-parse --abbrev-ref HEAD > git-branch'
        env.GIT_COMMIT = readFile('git-commit')
        env.BRANCH_TAG = env.BRANCH_NAME.replaceAll('/', '-')

        // Print Environment
        sh 'env | sort'

        stage 'Build base image'

        sh "docker build -t ${repo}duck1123/jiksnu-base:${env.BRANCH_TAG} docker/jiksnu-base"
        sh "docker tag ${repo}duck1123/jiksnu-base:${env.BRANCH_TAG} ${repo}duck1123/jiksnu-base:latest"
        sh "docker push ${repo}duck1123/jiksnu-base:${env.BRANCH_TAG}"
        sh "docker push ${repo}duck1123/jiksnu-base:latest"

        stage 'Build ruby image'

        sh "docker build -t ${repo}duck1123/jiksnu-ruby-base:${env.BRANCH_TAG} docker/jiksnu-ruby-base"
        sh "docker tag ${repo}duck1123/jiksnu-ruby-base:${env.BRANCH_TAG} ${repo}duck1123/jiksnu-ruby-base:latest"
        sh "docker push ${repo}duck1123/jiksnu-ruby-base:${env.BRANCH_TAG}"
        sh "docker push ${repo}duck1123/jiksnu-ruby-base:latest"

        stage 'Unit Tests'

        try {
            sh "docker-compose up -d mongo > mongo_container_id"

            sh "docker inspect workspace_mongo_1 | jq -r '.[].NetworkSettings.Networks.workspace_default.IPAddress' | tee jiksnu_db_host"
            env.JIKSNU_DB_HOST = readFile('jiksnu_db_host').trim()

            sh "docker inspect workspace_mongo_1 | jq -r '.[].NetworkSettings.Ports | keys | .[] | split(\"/\")[0]' | tee jiksnu_db_port"
            env.JIKSNU_DB_PORT = readFile('jiksnu_db_port').trim()

            sh 'script/cibuild'

            step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/TEST-*.xml'])
        } catch (caughtError) {
            err = caughtError
        } finally {
            sh 'docker-compose stop'

            if (err) {
                throw err
            }
        }

        stage 'Build jars'

        sh 'lein install'
        sh 'lein uberjar'
        archive 'target/*jar'

        stage 'Build image'

        sh 'docker-compose build web-dev'
        sh "docker push ${repo}duck1123/jiksnu:dev"
        sh "docker build -t ${repo}duck1123/jiksnu:${env.BRANCH_TAG} ."
        sh "docker tag ${repo}duck1123/jiksnu:${env.BRANCH_TAG} ${repo}duck1123/jiksnu:latest"
        sh "docker push ${repo}duck1123/jiksnu:${env.BRANCH_TAG}"
        sh "docker push ${repo}duck1123/jiksnu:latest"

        stage 'Generate Reports'

        sh 'lein doc'
        step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
        step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])

        // stage 'Integration tests'

        // try {
        //     sh 'docker-compose up -d webdriver'
        //     sh 'docker-compose up -d jiksnu-integration'

        //     sh "docker inspect workspace_jiksnu-integration_1 | jq -r '.[].NetworkSettings.Networks.workspace_default.IPAddress' | tee jiksnu_host"
        //     env.JIKSNU_HOST = readFile('jiksnu_host').trim()

        //     sh "until \$(curl --output /dev/null --silent --fail http://${env.JIKSNU_HOST}/status); do echo '.'; sleep 5; done"
        //     sh 'docker-compose run --rm web-dev script/protractor'
        // } catch (caughtError) {
        //     err = caughtError
        // } finally {
        //     sh 'docker-compose stop'
        //     sh 'docker-compose rm -f'

        //     if (err) {
        //         throw err
        //     }
        // }
    }
}
