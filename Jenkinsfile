#!groovy

def org = 'kronkltd'
def project = 'jiksnu'

def repo = 'repo.jiksnu.org/'
def repoCreds = '8bb2c76c-133c-4c19-9df1-20745c31ac38'
def repoPath = "https://${repo}"

def clojureImage, devImage, err, mainImage, mongoContainer

def pushImages = false
def integrationTests = false

// Set build properties
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '5']],
            [$class: 'GithubProjectProperty', displayName: 'Jiksnu', projectUrlStr: "https://github.com/${org}/${project}/"]]);

stage('Prepare Environment') {
    node('docker') {
        step([$class: 'WsCleanup'])

        // Set current git commit
        checkout scm

        env.BUILD_TAG = env.BUILD_TAG.replaceAll('%2F', '-')

        def isPR = false

        if (env.CHANGE_ID) {
            echo "PR build detected due to change id"
            isPR = true
        }

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
}

stage('Build Dev Image') {
    node('docker') {
        ansiColor('xterm') {
            devImage = docker.build("${org}/${project}-dev:${env.BRANCH_TAG}",
                                    "-f docker/web-dev/Dockerfile .")
            if (pushImages) {
                docker.withRegistry(repoPath, repoCreds) {
                    devImage.push()
                }
            }
        }
    }
}

stage('Unit Tests') {
    node('docker') {
        try {
            mongoContainer = docker.image('mongo').run("--name ${env.BUILD_TAG}-mongo")

            devImage.inside(["--link ${mongoContainer.id}:mongo",
                             "--name ${env.BUILD_TAG}-dev"].join(' ')) {
                checkout scm

                ansiColor('xterm') {
                    timestamps {
                        sh 'script/cibuild'
                    }
                }
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
}

stage('Build Jars') {
    node('docker') {
        devImage.inside("--name ${env.BUILD_TAG}-jars") {
            checkout scm
            sh 'lein install'
            sh 'lein uberjar'
            archive 'target/*jar'
        }
    }
}

stage('Build Run Image') {
    node('docker') {
        ansiColor('xterm') {
            sh "sigil -f Dockerfile.tmpl -p > Dockerfile"

            mainImage = docker.build("${org}/${project}:${env.BRANCH_TAG}")

            if (pushImages) {
                docker.withRegistry(repoPath, repoCreds) {
                    mainImage.push()
                }
            }
        }
    }
}

stage('Generate Reports') {
    node('docker') {
        devImage.inside("--name ${env.BUILD_TAG}-reports") {
            checkout scm

            ansiColor('xterm') {
                sh 'lein doc'
            }

            step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
            step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])
        }
    }
}

if (integrationTests) {
    stage('Integration tests') {
        node('docker') {
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
