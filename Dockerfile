FROM navikt/java:17

ADD --chown=1069:1069 .nais/*.sh /init-scripts/
ADD --chown=1069:1069 target/basta*.war /app/root.war

USER 1069
