# openshift-jee-sample (pi-ant version)

A sample app to be deployed on openshift environments

## Prequisites

- a working OpenShift Cluster
- [Tiller](https://blog.openshift.com/getting-started-helm-openshift/)
- OpenShift Client Tools installed

## Build Image

    mvn install

## Deployment

Development Environment:
    
    helm upgrade dev src/main/helm/sampleapp  -f src/main/helm/dev.yaml --install

Production Environment:

    helm upgrade pro src/main/helm/sampleapp  -f src/main/helm/pro.yaml --install