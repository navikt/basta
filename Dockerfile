FROM navikt/java:8

RUN umask o+r

COPY .nais/export-vault-secrets.sh /init-scripts
RUN chmod +x /init-scripts/export-vault-secrets.sh
COPY .nais/run-java.sh /run-java.sh
RUN chmod +x /run-java.sh
RUN groupadd -r basta && useradd -r -s /bin/false -g basta basta
COPY target/*.war "/app/root.war"
RUN chown -R basta:basta /app
USER basta
