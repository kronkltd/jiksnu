#!groovy

def org = 'kronkltd'
def project = 'jiksnu'

def repo, repoCreds, repoPath
def dbImage, devImage, err, mainImage, mongoContainer

def pushImages = true
def integrationTests = false

node('docker') {
    ansiColor('xterm') {
        timestamps {
            stage('Init') {
                // Set current git commit
                checkout scm

                env.BUILD_TAG = env.BUILD_TAG.replaceAll('%2F', '-')
                repo = env.JENKINS_DOCKER_REGISTRY_HOST
                repoCreds = '8bb2c76c-133c-4c19-9df1-20745c31ac38'
                repoPath = "https://${repo}"

                properties([[$class: 'BuildDiscarderProperty',
                               strategy: [$class: 'LogRotator', numToKeepStr: '5']]]);

                if (env.BRANCH_NAME == 'develop') {
                    env.BRANCH_TAG = 'latest'
                } else if (env.BRANCH_NAME == 'master') {
                    // TODO: Parse version numbers
                    env.BRANCH_TAG = 'stable'
                } else {
                    env.BRANCH_TAG = env.BRANCH_NAME.replaceAll('/', '-')
                }

                dbImage = docker.image('mongo')

                // Print Environment
                sh 'env | sort'
            }

            stage('Build Dev Image') {
                devImage = docker.build("${repo}/${org}/${project}:${env.BRANCH_TAG}-dev",
                                        "-f docker/web-dev/Dockerfile .")
                if (pushImages) {
                    devImage.push()
                }
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

            stage('Build Run Image') {
                sh "sigil -f Dockerfile.tmpl -p > Dockerfile"

                mainImage = docker.build("${repo}/${org}/${project}:${env.BRANCH_TAG}")

                if (pushImages) {
                    mainImage.push()
                }
            }

            stage('Generate Reports') {
                devImage.inside("--name ${env.BUILD_TAG}-reports") {
                    sh 'lein doc'

                    step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
                    step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])
                }
            }

            if (integrationTests) {
                stage('Integration tests') {
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
