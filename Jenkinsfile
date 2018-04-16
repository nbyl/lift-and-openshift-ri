String getVersion() {
    def lastTag = sh(returnStdout: true, script: 'git describe --abbrev=0 --tags')
    def commitsSinceTag = sh(returnStdout: true, script: "git rev-list ${lastTag.trim()}.. --count")
    def commitId = sh(returnStdout: true, script: "git rev-parse --short HEAD")
    def buildTimestamp = new Date().format("yyyyMMddHHmmss")

    return "${lastTag.trim()}.${commitsSinceTag.trim()}-${buildTimestamp}-${commitId.trim()}"
}

def deployConfiguration(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    dir('config') {
        git(
                url: config.gitUrl,
                branch: config.gitBranch
        )

        sh("oc create secret generic ${config.name}-config --from-file=./ --dry-run=true -o yaml | oc apply -f -")
    }
}

def mavenNode(Map parameters = [:], body) {
    def cloud = 'openshift'

    def dockerImage = 'docker:dind'
    def jnlpImage = 'openshift/jenkins-slave-maven-centos7:v3.10'

    podTemplate(cloud: cloud,
            label: 'maven',
            serviceAccount: 'jenkins',
            containers: [
                    containerTemplate(
                            name: 'jnlp',
                            image: "${jnlpImage}",
                            args: '${computer.jnlpmac} ${computer.name}',
                            workingDir: '/home/jenkins/',
                            resourceLimitMemory: '1024Mi',
                            envVars: [
                                    envVar(key: 'DOCKER_HOST', value: 'tcp://localhost:2375'),
                            ]),
                    containerTemplate(
                            name: 'docker',
                            image: "${dockerImage}",
                            resourceLimitMemory: '640Mi')
            ]) {
        node('maven') {
            sh 'echo hello world!'
        }
        //body()
    }
}

try {
    timeout(time: 20, unit: 'MINUTES') {
        mavenNode {
            //node('maven') {
            def mavenCliOptions = env.MAVEN_CLI_OPTIONS ? env.MAVEN_CLI_OPTIONS : "-B"

            def releaseVersion = "1.0.${env.BUILD_NUMBER}"
            def applicationName = "laor"

            def configRepositoryUrl = env.CONFIG_REPOSITORY_URL ? env.CONFIG_REPOSITORY_URL : "https://github.com/nbyl/container-configurator.git"

            stage('Prepare Build') {
                dir('scm') {
                    //checkout scm
                    checkout([
                            $class                           : 'GitSCM',
                            branches                         : scm.branches,
                            doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
                            extensions                       : scm.extensions + [[$class: 'CloneOption', noTags: false, reference: '']],
                            submoduleCfg                     : [],
                            userRemoteConfigs                : scm.userRemoteConfigs
                    ])
                    releaseVersion = getVersion()

                    sh("./mvnw ${mavenCliOptions} org.codehaus.mojo:versions-maven-plugin:2.2:set -U -DnewVersion=${releaseVersion}")
                }
            }

            stage('Test') {
                dir('scm') {
                    //try {
                    // TODO: re-activate tests on mega jenkins
                    //sh("./mvnw ${mavenCliOptions} test")
                    //} finally {
                    //    junit 'impl/**/target/surefire-reports/*.xml'
                    //}
                }
            }

            // TODO: sonar, versioneye, ...

            stage('Build Docker Image') {
                dir('scm') {
                    sh("./mvnw ${mavenCliOptions} install -DskipTests")
                }
            }

            stage('Integration Test - deploy configuration') {
                deployConfiguration {
                    gitUrl = configRepositoryUrl
                    gitBranch = 'master'
                    name = "${applicationName}-stage"
                }
            }

            stage('Integration Test - deploy application') {
                dir('scm') {
                    sh("oc process -f src/main/openshift/application-template.yaml -p APPLICATION_NAME=${applicationName}-stage -p IMAGE_VERSION=${releaseVersion}| oc apply -f -")
                    openshiftDeploy(depCfg: "${applicationName}-stage")
                }
            }

            stage('Integration Test - run tests') {
                dir('scm') {
                    sh("mvn -B org.apache.maven.plugins:maven-failsafe-plugin:integration-test org.apache.maven.plugins:maven-failsafe-plugin:verify -P acceptance-test -DacceptanceTest.baseUri=http://laor-stage")
                }
            }

            stage('Integration Test - teardown stage') {
                dir('scm') {
                    sh("oc process -f src/main/openshift/application-template.yaml -p APPLICATION_NAME=${applicationName}-stage -p IMAGE_VERSION=${releaseVersion}| oc delete -f -")
                }
            }

            // TODO: push image to artifactory here

//            stage('Ask for promotion') {
//                input "Do you want to deploy ${applicationName} to production?"
//            }

            stage('Production - deploy configuration') {
                deployConfiguration {
                    gitUrl = configRepositoryUrl
                    gitBranch = 'master'
                    name = "${applicationName}"
                }
            }
            stage('Production - deploy application') {
                dir('scm') {
                    sh("oc process -f src/main/openshift/application-template.yaml -p APPLICATION_NAME=${applicationName} -p IMAGE_VERSION=${releaseVersion}| oc apply -f -")
                    openshiftDeploy(depCfg: "${applicationName}")
                }
            }

        }
    }
} catch (err) {
    echo "in catch block"
    echo "Caught: ${err}"
    currentBuild.result = 'FAILURE'
    throw err
}
