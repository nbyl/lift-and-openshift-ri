try {
    timeout(time: 20, unit: 'MINUTES') {
        node('maven') {

            stage('build') {
                // TODO: we should really use the SHA1 commit hash here.
                def releaseVersion = "1.0.${env.BUILD_NUMBER}"

                //sh('env | sort')
                checkout scm

                sh("mvn org.codehaus.mojo:versions-maven-plugin:2.2:set -U -DnewVersion=${releaseVersion}")
                sh('mvn package fabric8:build')
                //openshiftBuild(buildConfig: 'nodejs-mongodb-example', showBuildLogs: 'true')
            }
            stage('config') {
                git(
                    //url: 'git@github.com:nbyl/container-configurator.git',
                    url: 'https://github.com/nbyl/container-configurator.git',
                    //credentialsId: 'minishift-github',
                    branch: 'master',
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'target/configuration']]
                )
                sh('oc delete secret ribn-dev-pi-config-secret --ignore-not-found=true')
                sh('oc create secret generic ribn-dev-pi-config-secret --from-file=./target/configuration/configuration/environment.properties,./target/configuration/configuration/app/standalone/configuration/sso/sso.keystore')
            }
            stage('deploy') {
                sh('find .')
                sh('oc process -f src/main/openshift/application-template.yaml | oc apply -f -')
                //openshiftDeploy(deploymentConfig: 'nodejs-mongodb-example')
            }
        }
    }
} catch (err) {
    echo "in catch block"
    echo "Caught: ${err}"
    currentBuild.result = 'FAILURE'
    throw err
}
