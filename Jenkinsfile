#!groovy

node {
    wrap([$class: 'AnsiColorBuildWrapper']) {
        stage 'Prepare Environment'

        // Set the path
        env.PATH = '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'

        // Set current git commit
        checkout scm
        sh 'git submodule sync'
        sh "git rev-parse HEAD | tr -d '\n' > git-commit"
        env.GIT_COMMIT = readFile('git-commit')
        echo "GIT_COMMIT=${env.GIT_COMMIT}"

        // Set build properties
        properties([[$class: 'GithubProjectProperty', displayName: 'Jiksnu', projectUrlStr: 'https://github.com/duck1123/jiksnu/'],
                    [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false]])

        // Print Environment
        sh 'env'

        stage 'Build base image'

        // Build jiksnu-base
        sh "docker build -t repo.jiksnu.org/duck1123/jiksnu-base:${env.GIT_COMMIT} docker/jiksnu-base"
        sh "docker push repo.jiksnu.org/duck1123/jiksnu-base:${env.GIT_COMMIT}"

        echo 'tag as latest'
        sh "docker tag repo.jiksnu.org/duck1123/jiksnu-base:${env.GIT_COMMIT} repo.jiksnu.org/duck1123/jiksnu-base:latest"
        sh 'docker push repo.jiksnu.org/duck1123/jiksnu-base:latest'

        stage 'Build ruby image'

        sh "docker build -t repo.jiksnu.org/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT} docker/jiksnu-ruby-base"
        sh "docker push repo.jiksnu.org/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT}"

        echo 'tag as latest'
        sh "docker tag repo.jiksnu.org/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT} repo.jiksnu.org/duck1123/jiksnu-ruby-base:latest"
        sh 'docker push repo.jiksnu.org/duck1123/jiksnu-ruby-base:latest'

        stage 'Unit Tests'

        def err

        try {
            sh "docker-compose up -d mongo > mongo_container_id"

            sh "docker inspect workspace_mongo_1 | jq '.[].NetworkSettings.Networks.workspace_default.IPAddress' | tr -d '\"' | tr -d '\n' > jiksnu_db_host"
            env.JIKSNU_DB_HOST = readFile('jiksnu_db_host')

            sh "docker inspect workspace_mongo_1 | jq '.[].NetworkSettings.Ports | keys | .[] | split(\"/\")[0]' | tr -d '\"' | tr -d '\n' > jiksnu_db_port"
            env.JIKSNU_DB_PORT = readFile('jiksnu_db_port')

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

        sh "docker build -t repo.jiksnu.org/duck1123/jiksnu:${env.GIT_COMMIT} ."
        sh "docker tag repo.jiksnu.org/duck1123/jiksnu:${env.GIT_COMMIT} repo.jiksnu.org/duck1123/jiksnu:latest"
        sh "docker push repo.jiksnu.org/duck1123/jiksnu:${env.GIT_COMMIT}"
        sh 'docker push repo.jiksnu.org/duck1123/jiksnu:latest'

        stage 'Generate Reports'

        step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])

        sh 'lein doc'

        step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])

        // stage 'Integration tests'

        // try {     
        //     // sh 'docker run -d --name pipeline_mongo_1 mongo'

        //     // sh 'docker run -d --name pipeline_jiksnu_1 --link pipeline_mongo_1:mongo repo.jiksnu.org/duck1123/jiksnu:latest'

        //     sh 'docker-compose up -d webdriver'
        //     sh 'docker-compose up -d jiksnu-integration'

        //     sh 'docker-compose run web-dev lein protractor'
        // } catch (caughtError) {
        //     err = caughtError
        // } finally {
        //     // sh 'docker stop pipeline_mongo_1'
        //     // sh 'docker rm pipeline_mongo_1'
        //     // sh 'docker stop pipeline_jiksnu_1'
        //     // sh 'docker rm pipeline_jiksnu_1'
        //     sh 'docker-compose stop'
        //     sh 'docker-compose rm -f'
            
        //     if (err) {
        //         throw err
        //     }
        // }
    }
}
