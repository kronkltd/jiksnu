#!/usr/bin/env groovy

def org = 'kronkltd'
def project = 'jiksnu'

def dbImage, devImage, err, mainImage, repo

def integrationTests = false

node('docker') {
  ansiColor('xterm') {
    timestamps {
      stage('Init') {
        // Set current git commit
        checkout scm

        env.BUILD_TAG = env.BUILD_TAG.replaceAll('%2F', '-')
        repo = "${env.DOCKER_REGISTRY_HOST}/"

        if (env.BRANCH_NAME == 'develop') {
          env.BRANCH_TAG = 'latest'
        } else if (env.BRANCH_NAME == 'master') {
          // TODO: Parse version numbers
          env.BRANCH_TAG = 'stable'
        } else {
          env.BRANCH_TAG = env.BRANCH_NAME.replaceAll('/', '-')
        }

        dbImage = docker.image('mongo')
        dbImage.pull()

        // Print Environment
        sh 'env | sort'
      }

      stage('Dev Image') {
        devImage = docker.build(
          "${repo}${org}/${project}:${env.BRANCH_TAG}-dev",
          ["--label net.kronkltd.built-by=${env.BUILD_TAG}", '.'].join(' '))
        devImage.push()
      }

      stage('Unit Tests') {
        dbImage.withRun("--name ${env.BUILD_TAG}-mongo") {
          mongoContainer ->
            devImage.inside(["--link ${mongoContainer.id}:mongo",
                             "--name ${env.BUILD_TAG}-dev"].join(' ')) {
              sh 'script/update'
              sh 'script/compile'
              sh 'script/cibuild'
            }
        }

        archive 'target/*jar'
        stash name: 'jars', includes: 'target/*.jar'
        junit 'target/surefire-reports/TEST-*.xml'
      }

      stage('Production Image') {
        unstash 'jars'

        sh 'cp target/jiksnu-*-standalone.jar jiksnu.jar'

        mainImage = docker.build(
          "${repo}${org}/${project}:${env.BRANCH_TAG}",
          ['-f Dockerfile.run',
           "--label net.kronkltd.built-by=${env.BUILD_TAG}", '.'].join(' '))
        mainImage.push()
      }

      stage('Generate Reports') {
        devImage.inside("--name ${env.BUILD_TAG}-reports") {
          sh 'lein doc'

          step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
          step([$class: 'TasksPublisher',
               high: 'FIXME',
               normal: 'TODO',
               pattern: '**/*.clj,**/*.cljs'])
        }
      }

      if (integrationTests) {
        stage('Integration Tests') {
          mainImage.withRun() {
            integrationContainer ->
              mainImage.inside {
                sh 'script/test-integration'
              }
          }
        }
      }
    }
  }
}
