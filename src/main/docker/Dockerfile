FROM registry.access.redhat.com/jboss-eap-7/eap71-openshift:latest

USER root:root
RUN chmod -R 777 /opt/eap/standalone/
USER wildfly

COPY maven/ /opt/eap/standalone/deployments/
