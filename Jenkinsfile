#!groovy

node {

    properties([[$class: 'GithubProjectProperty',
        displayName: 'Jiksnu',
        projectUrlStr: 'https://github.com/duck1123/jiksnu/'],
        [$class: 'RebuildSettings',
        autoRebuild: false,
        rebuildDisabled: false]])

    wrap([$class: 'AnsiColorBuildWrapper']) {
        env.PATH = '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
        sh "git rev-parse HEAD | tr -d '\n' > git-commit"
        env.GIT_COMMIT = readFile('git-commit')

        stage 'Print Environment'

        sh 'env'

        stage 'Checkout'

        checkout scm
        sh 'git submodule sync'

        stage 'Build base image'

        sh "docker build -t repo.jiksnu.org/duck1123/jiksnu-base:${env.GIT_COMMIT} docker/jiksnu-base"
        sh "docker tag repo.jiksnu.org/duck1123/jiksnu-base:${env.GIT_COMMIT} repo.jiksnu.org/duck1123/jiksnu-base:latest"
        sh "docker push repo.jiksnu.org/duck1123/jiksnu-base:${env.GIT_COMMIT}"
        sh 'docker push repo.jiksnu.org/duck1123/jiksnu-base:latest'

        stage 'Build ruby image'

        sh "docker build -t repo.jiksnu.org/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT} docker/jiksnu-ruby-base"
        sh "docker tag repo.jiksnu.org/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT} repo.jiksnu.org/duck1123/jiksnu-ruby-base:latest"
        sh "docker push repo.jiksnu.org/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT}"
        sh 'docker push repo.jiksnu.org/duck1123/jiksnu-ruby-base:latest'

        stage 'Unit Tests'

        def err

        try {
            sh "docker-compose up -d mongo"

            // sleep 15

            sh "docker inspect workspace_mongo_1 | jq '.[].NetworkSettings.Networks.workspace_default.IPAddress' | tr -d '\"' | tr -d '\n' > jiksnu_db_host"
            sh "docker inspect workspace_mongo_1 | jq '.[].NetworkSettings.Ports | keys | .[] | split(\"/\")[0]' | tr -d '\"' | tr -d '\n' > jiksnu_db_port"

            env.JIKSNU_DB_HOST = readFile('jiksnu_db_host')
            env.JIKSNU_DB_PORT = readFile('jiksnu_db_port')

            sh 'script/cibuild'

            step([$class: 'JUnitResultArchiver',
                 testDataPublishers: [[$class: 'StabilityTestDataPublisher']],
                 testResults: 'target/surfire-reports/*.xml'])
        } catch (caughtError) {
            err = caughtError
        } finally {
            sh 'docker-compose stop'

            if (err) {
                throw err
            }
        }

        stage 'Build jar'

        sh 'lein install'
        archive 'target/*jar'

        stage 'Build uberjar'

        sh 'lein uberjar'
        archive 'target/*jar'

        stage 'Build image'

        sh "docker build -t repo.jiksnu.org/duck1123/jiksnu:${env.GIT_COMMIT} ."
        sh "docker tag repo.jiksnu.org/duck1123/jiksnu:${env.GIT_COMMIT} repo.jiksnu.org/duck1123/jiksnu:latest"
        sh "docker push repo.jiksnu.org/duck1123/jiksnu:${env.GIT_COMMIT}"
        sh 'docker push repo.jiksnu.org/duck1123/jiksnu:latest'

        stage 'Build metrics'

        step([$class: 'TasksPublisher',
             canComputeNew: false,
             defaultEncoding: '',
             excludePattern: '',
             healthy: '',
             high: 'FIXME',
             low: '',
             normal: 'TODO',
             pattern: '**/*.clj,**/*.cljs',
             unHealthy: ''])

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
