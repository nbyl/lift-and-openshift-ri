# lift-and-openshift-ri

A sample app to be deployed on openshift environments

## Prequisites

- a working OpenShift Cluster, e.g. [MiniShift](https://github.com/minishift/minishift)
- OpenShift Client Tools installed

## Setup

### MiniShift

If you want to use MiniShift please use at least 12 GB of RAM:

        minishift start --vm-driver virtualbox --memory 12288MB

### Pipeline

First, login and create a project:

        oc login
        oc new-project lift-and-openshift-ri --display-name="Lift & OpenShift - Reference Implementation"

 To setup the pipeline, process the template into cluster. This will create a pipeline which will use the Jenkinsfile from the git repository for further processing:

        oc process -f src/main/openshift/pipeline.yaml | oc apply -f -

Afterwards, you can start the pipeline from the Web UI.

## Development

To make further developments in the build pipeline, you can create your own branch and build the project against it. To build a branch called `nbyl-devel`, create a new build configuration like this.

        oc process -f src/main/openshift/pipeline.yaml -p NAME=devel -p BRANCH=nbyl-devel | oc apply -f -

# Documentation

## Versioning

The version of the image to be built is derived at build time and uses the following convention:

    (API Version).(Commits since last change of API version)-(Timestamp of Build)-(short hash of the git commit)

The versioning follows the [Semantic Versioning Standard 2.0](https://semver.org). The MAJOR and MINOR are representing the external API of the application. They are derived from a the last git tag. The MINOR version is the number of commits since the last version tag. So if you change the external API of the application. Please re-tag the git branch.

An example: The current version of the external API is 2.3. You make a change that adds a backwards-compatible field. This changes the external API and you should upgrade your API version to 2.4:

        git tag 2.4
        git push --tags
        git push

Afterwards your version is available on your git server.
