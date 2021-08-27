FROM navikt/java:8

ADD --chown=1069:1069 .nais/*.sh /init-scripts/
ADD --chown=1069:1069 target/root.war /app/

USER 1069
