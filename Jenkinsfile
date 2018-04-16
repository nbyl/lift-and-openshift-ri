String getVersion() {
    def lastTag = sh(returnStdout: true, script: 'git describe --abbrev=0 --tags')
    def commitsSinceTag = sh(returnStdout: true, script: "git rev-list ${lastTag.trim()}.. --count")
    def commitId = sh(returnStdout: true, script: "git rev-parse --short HEAD")
    def buildTimestamp = new Date().format("yyyyMMddHHmmss")

    return "${lastTag.trim()}.${commitsSinceTag.trim()}-${buildTimestamp}-${commitId.trim()}"
}

try {
    timeout(time: 20, unit: 'MINUTES') {
        node('maven') {
            def mavenCliOptions = env.MAVEN_CLI_OPTIONS ? env.MAVEN_CLI_OPTIONS : "-B"

            def releaseVersion = "1.0.${env.BUILD_NUMBER}"
            def applicationName = "laor"

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
                dir('config') {
                    git(
                            url: 'https://github.com/nbyl/container-configurator.git',
                            branch: 'master'
                    )
                    sh("oc delete secret ${applicationName}-stage-config --ignore-not-found=true")
                    sh("oc create secret generic ${applicationName}-stage-config --from-file=./configuration/environment.properties,./configuration/app/standalone/configuration/sso/sso.keystore")
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
                dir('config') {
                    git(
                            url: 'https://github.com/nbyl/container-configurator.git',
                            branch: 'master'
                    )
                    sh("oc delete secret ${applicationName}-config --ignore-not-found=true")
                    sh("oc create secret generic ${applicationName}-config --from-file=./configuration/environment.properties,./configuration/app/standalone/configuration/sso/sso.keystore")
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
