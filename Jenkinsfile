try {
    timeout(time: 20, unit: 'MINUTES') {
        node('maven') {

            def releaseVersion = "1.0.${env.BUILD_NUMBER}"
            stage('build') {
                // TODO: we should really use the SHA1 commit hash here.

                dir('scm') {
                    checkout scm

                    sh("mvn -B org.codehaus.mojo:versions-maven-plugin:2.2:set -U -DnewVersion=${releaseVersion}")
                    sh('mvn -B package fabric8:build')
                }
            }
            stage('config') {
                //dir('config') {
                //    git(
                //        url: 'https://github.com/nbyl/container-configurator.git',
                //        branch: 'master'
                //    )
                //    sh('oc delete secret ribn-dev-pi-config-secret --ignore-not-found=true')
                //    sh('oc create secret generic ribn-dev-pi-config-secret --from-file=./configuration/environment.properties,./configuration/app/standalone/configuration/sso/sso.keystore')
                }
            }
            stage('deploy') {
                dir('scm') {
                    sh("oc process -f src/main/openshift/application-template.yaml -p IMAGE_VERSION=${releaseVersion}| oc apply -f -")
                    openshiftDeploy(depCfg: 'lift-and-openshift-ri')
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
