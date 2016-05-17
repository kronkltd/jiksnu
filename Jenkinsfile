#!groovy

node {
    wrap([$class: 'AnsiColorBuildWrapper']) {
        env.PATH = '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'

        stage 'Print Environment'

        sh 'env'

        stage 'Checkout'

        checkout scm
        sh 'git submodule sync'

        stage 'Build base image'

        sh 'cd docker/jiksnu-base'
        sh "docker build -t registry.kronkltd.net:5000/duck1123/jiksnu-base:${env.GIT_COMMIT} ."
        sh "docker tag registry.kronkltd.net:5000/duck1123/jiksnu-base:${env.GIT_COMMIT} registry.kronkltd.net:5000/duck1123/jiksnu-base:latest"
        sh "docker push registry.kronkltd.net:5000/duck1123/jiksnu-base:${env.GIT_COMMIT}"
        sh 'docker push registry.kronkltd.net:5000/duck1123/jiksnu-base:latest'

        stage 'Build ruby image'

        sh 'cd docker/jiksnu-ruby-base'
        sh "docker build -t registry.kronkltd.net:5000/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT} ."
        sh "docker tag registry.kronkltd.net:5000/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT} registry.kronkltd.net:5000/duck1123/jiksnu-ruby-base:latest"
        sh "docker push registry.kronkltd.net:5000/duck1123/jiksnu-ruby-base:${env.GIT_COMMIT}"
        sh 'docker push registry.kronkltd.net:5000/duck1123/jiksnu-ruby-base:latest'

        stage 'Unit Tests'

        sh 'script/cibuild'

        stage 'Build jar'

        sh 'lein install'
        archive 'target/*jar'

        stage 'Build uberjar'

        sh 'lein uberjar'
        archive 'target/*jar'

        stage 'Build image'

        sh "docker build -t registry.kronkltd.net:5000/duck1123/jiksnu:${env.GIT_COMMIT} ."
        sh "docker tag registry.kronkltd.net:5000/duck1123/jiksnu:${env.GIT_COMMIT} registry.kronkltd.net:5000/duck1123/jiksnu:latest"
        sh "docker push registry.kronkltd.net:5000/duck1123/jiksnu:${env.GIT_COMMIT}"
        sh 'docker push registry.kronkltd.net:5000/duck1123/jiksnu:latest'

        // stage 'Integration tests'

        // try {     
        //     // sh 'docker run -d --name pipeline_mongo_1 mongo'

        //     // sh 'docker run -d --name pipeline_jiksnu_1 --link pipeline_mongo_1:mongo registry.kronkltd.net:5000/duck1123/jiksnu:latest'

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
