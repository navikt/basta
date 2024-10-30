FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --chown=1069:1069 init-scripts /init-scripts
COPY --chown=1069:1069 .nais/*.sh /init-scripts/
COPY --chown=1069:1069 target/basta*.war /app.war

ENV LC_ALL="nb_NO.UTF-8"
ENV LANG="nb_NO.UTF-8"
ENV TZ="Europe/Oslo"
ENV APP_BINARY=app
ENV APP_JAR=app.jar
ENV MAIN_CLASS="Main"

USER 1069

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.war"]
