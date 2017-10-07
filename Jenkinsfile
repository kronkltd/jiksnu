#!/usr/bin/env groovy

def org = 'kronkltd'
def project = 'jiksnu'

def dbImage, devImage, err, integrationTests, mainImage, repo, shouldDeploy, shouldDeployArtifacts,
    shouldDeployImages, shouldGenerateDocs

node('docker') {
  ansiColor('xterm') {
    timestamps {
      stage('Init') {
        cleanWs()

        // Set current git commit
        checkout scm

        env.BUILD_TAG = env.BUILD_TAG.replaceAll('%2F', '-')
        repo = "${env.DOCKER_REGISTRY_HOST}/"

        env.CI_PROJECT_NAMESPACE = org
        env.CI_PROJECT_NAME = project

        if (env.BRANCH_NAME == 'develop') {
          env.BRANCH_TAG = 'latest'
        } else if (env.BRANCH_NAME == 'master') {
          // TODO: Parse version numbers
          env.BRANCH_TAG = 'stable'
        } else {
          env.BRANCH_TAG = env.BRANCH_NAME.replaceAll('/', '-')
        }

        env.IMAGE_TAG = "${repo}${org}/${project}:${env.BRANCH_TAG}"

        integrationTests = false
        shouldDeploy = (env.BRANCH_TAG == 'latest')
        shouldDeployArtifacts = (env.BRANCH_TAG == 'latest') || (env.BRANCH_TAG == 'stable')
        shouldDeployImages = (env.BRANCH_TAG == 'latest') || (env.BRANCH_TAG == 'stable')

        dbImage = docker.image('mongo')
        dbImage.pull()

        // Print Environment
        sh 'env | sort'
      }

      stage('Dev Image') {
        def devOptions = ["--label net.kronkltd.built-by=${env.BUILD_TAG}", '.'].join(' ')

        devImage = docker.build("${env.IMAGE_TAG}-dev", devOptions)

        if (shouldDeployImages) {
          devImage.push()
        }
      }

      stage('Unit Tests') {
        def dbOptions = "--name ${env.BUILD_TAG}-mongo"

        dbImage.withRun(dbOptions) {mongoContainer ->
          def devOptions = ["--link ${mongoContainer.id}:mongo",
                            "--name ${env.BUILD_TAG}-dev"].join(' ')

          devImage.inside(devOptions) {
            sh 'script/update'
            sh 'script/compile'
            sh 'script/cibuild'
            sh 'script/compile-production'
          }
        }

        archive 'target/*jar'
        stash name: 'jars', includes: 'target/*.jar'
        junit 'target/surefire-reports/TEST-*.xml'
      }

      stage('Production Image') {
        unstash 'jars'

        sh 'cp target/jiksnu-*-standalone.jar jiksnu.jar'

        def mainOptions = ['-f Dockerfile.run',
                           "--label net.kronkltd.built-by=${env.BUILD_TAG}", '.'].join(' ')

        mainImage = docker.build("${env.IMAGE_TAG}", mainOptions)

        if (shouldDeployImages) {
          mainImage.push()
        }
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
          def seleniumImage = docker.image('elgalu/selenium')
          seleniumImage.pull()
          def seleniumOpts = ["-e VNC_PASSWORD=hunter2"].join(' ')

          seleniumImage.withRun(seleniumOpts) {seleniumContainer ->

            sleep 20

            dbImage.withRun() {dbContainer ->
              sleep 20

              def mainOpts = ["--link ${dbContainer.id}:mongo"].join(' ')

              mainImage.withRun(mainOpts) {mainContainer ->
                sleep 20

                def devOpts = ["--link ${seleniumContainer.id}:selenium",
                               "--link ${mainContainer.id}:jiksnu-dev"].join(' ')

                devImage.inside(devOpts) {integrationContainer ->
                  sleep 20

                  sh 'script/test-integration'
                }
              }
            }
          }
        }
      }
    }
  }
}
