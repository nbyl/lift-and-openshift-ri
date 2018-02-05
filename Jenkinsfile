try {
    timeout(time: 20, unit: 'MINUTES') {
        node('maven') {
            stage('build') {
                checkout scm
                sh('mvn package')
                //openshiftBuild(buildConfig: 'nodejs-mongodb-example', showBuildLogs: 'true')
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
