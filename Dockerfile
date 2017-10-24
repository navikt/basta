FROM docker.adeo.no:5000/openjdk:8-jre-alpine

ARG version
ARG app_name

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"
ENV DEFAULT_JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

COPY target/$app_name-$version.war "/app/root.war"

WORKDIR /app

EXPOSE 8080

CMD java -jar $DEFAULT_JAVA_OPTS $JAVA_OPTS /app/root.war
