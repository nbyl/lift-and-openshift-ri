FROM centos/wildfly:latest

USER root:root
RUN chmod -R 777 /opt/wildfly/standalone/
USER wildfly

COPY maven/ /opt/wildfly/standalone/deployments/

