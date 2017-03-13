#!groovy

def org = 'kronkltd'
def project = 'jiksnu'

def repo, repoCreds, repoPath
def clojureImage, devImage, err, mainImage, mongoContainer

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
                    mongoContainer = docker.image('mongo').run("--name ${env.BUILD_TAG}-mongo")

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
                    try {
                        sh 'docker-compose up -d webdriver'
                        sh 'docker-compose up -d jiksnu-integration'

                        sh "docker inspect workspace_jiksnu-integration_1 | jq -r '.[].NetworkSettings.Networks.workspace_default.IPAddress' | tee jiksnu_host"
                        env.JIKSNU_HOST = readFile('jiksnu_host').trim()

                        sh "until \$(curl --output /dev/null --silent --fail http://${env.JIKSNU_HOST}/status); do echo '.'; sleep 5; done"
                        sh 'docker-compose run --rm web-dev script/protractor'
                    } catch (caughtError) {
                        err = caughtError
                    } finally {
                        sh 'docker-compose stop'
                        sh 'docker-compose rm -f'

                        if (err) {
                            throw err
                        }
                    }
                }
            }
        }
    }
}
