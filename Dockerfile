FROM azul/zulu-openjdk-alpine:21

RUN wget -O /dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.2.1/dumb-init_1.2.1_amd64
RUN echo "057ecd4ac1d3c3be31f82fc0848bf77b1326a975b4f8423fe31607205a0fe945 /dumb-init" | sha256sum -c
RUN chmod +x /dumb-init
RUN ln -s /usr/share/zoneinfo/Europe/Oslo /etc/localtimes

COPY --chown=1069:1069 init-scripts /init-scripts
COPY --chown=1069:1069 --chmod=700 /entrypoint.sh /app/
COPY --chown=1069:1069 target/basta*.war /app/app.war

ENV APP_JAR=/app/app.war
ENV LC_ALL="nb_NO.UTF-8"
ENV LANG="nb_NO.UTF-8"
ENV TZ="Europe/Oslo"

WORKDIR /app

USER 1069

EXPOSE 8080

ENTRYPOINT ["/dumb-init", "--", "/app/entrypoint.sh"]
