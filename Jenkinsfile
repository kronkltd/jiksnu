#!/usr/bin/env groovy

def org = 'kronkltd'
def project = 'jiksnu'

def repo, repoCreds, repoPath
def dbImage, devImage, err, mainImage, mongoContainer

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
                devImage = docker.build("${repo}${org}/${project}:${env.BRANCH_TAG}-dev",
                                        ['-f docker/web-dev/Dockerfile',
                                         "--label net.kronkltd.built-by=${env.BUILD_TAG}",
                                         '.'].join(' '))
                devImage.push()
            }

            stage('Unit Tests') {
                try {
                    mongoContainer = dbImage.run("--name ${env.BUILD_TAG}-mongo")

                    devImage.inside(["--link ${mongoContainer.id}:mongo",
                                     "--name ${env.BUILD_TAG}-dev"].join(' ')) {
                        sh 'script/cibuild'
                    }

                    junit 'target/surefire-reports/TEST-*.xml'
                } catch (caughtError) {
                    err = caughtError
                } finally {
                    mongoContainer.stop()

                    if (err) {
                        throw err
                    }
                }
            }

            stage('Build Jars') {
                devImage.inside("--name ${env.BUILD_TAG}-jars") {
                    sh 'lein install'
                    sh 'lein uberjar'
                    archive 'target/*jar'
                    stash name: 'jars', includes: 'target/*.jar'
                }
            }

            stage('Production Image') {
                sh "sigil -f Dockerfile.tmpl -p > Dockerfile"

                mainImage = docker.build("${repo}${org}/${project}:${env.BRANCH_TAG}",
                                         ["--label net.kronkltd.built-by=${env.BUILD_TAG}",
                                          '.'].join(' '))
                mainImage.push()
            }

            stage('Generate Reports') {
                devImage.inside("--name ${env.BUILD_TAG}-reports") {
                    sh 'lein doc'

                    step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
                    step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])
                }
            }

            if (integrationTests) {
                stage('Integration Tests') {
                    def integrationContainer

                    try {
                        integrationContainer = mainImage.run()

                        mainImage.inside {
                            sh 'script/test-integration'
                        }
                    } catch (caughtError) {
                        err = caughtError
                    } finally {
                        integrationContainer.stop()

                        if (err) {
                            throw err
                        }
                    }
                }
            }
        }
    }
}
