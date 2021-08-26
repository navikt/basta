FROM navikt/java:8

COPY .nais/export-vault-secrets.sh /init-scripts
COPY .nais/run-java.sh /run-java.sh
COPY target/*.war "/app/root.war"
USER basta
