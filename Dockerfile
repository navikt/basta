FROM navikt/java:8

COPY .nais/export-vault-secrets.sh /init-scripts
COPY .nais/run-java.sh /run-java.sh
ADD --chown=1069:1069 * /init-scripts/
COPY target/*.war "/app/root.war"
ADD --chown=1069:1069 * /app/

USER 1069
