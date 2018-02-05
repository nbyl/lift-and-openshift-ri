try {
    timeout(time: 20, unit: 'MINUTES') {
        node('maven') {
            stage('build') {
                checkout scm
                sh('mvn package')
                //openshiftBuild(buildConfig: 'nodejs-mongodb-example', showBuildLogs: 'true')
            }
            stage('config') {
                git(
                    //url: 'git@github.com:nbyl/container-configurator.git',
                    url: 'https://github.com/nbyl/container-configurator.git',
                    //credentialsId: 'minishift-github',
                    branch: 'master'
                )
                sh('oc delete secret ribn-dev-pi-config-secret --ignore-not-found=true')
                sh('oc create secret generic ribn-dev-pi-config-secret --from-file=./configuration/environment.properties,./configuration/app/standalone/configuration/sso/sso.keystore')
            }
            stage('deploy') {
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
