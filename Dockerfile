FROM docker.adeo.no:5000/openjdk:8-jre-alpine

ARG version
ARG app_name

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"
ENV DEFAULT_JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENV JAVA_OPTS="$JAVA_OPTS -Dflyway.enabled=true"

COPY target/$app_name-$version.war "/app/root.war"

WORKDIR /app

EXPOSE 8080

ENV JAVA_PROXY_OPTS="-Dhttp.proxySet=true -Dhttps.proxySet=true -Dhttp.proxyHost=webproxy.nais -Dhttps.proxyHost=webproxy.nais -Dhttp.proxyPort=8088 -Dhttps.proxyPort=8088"

CMD java -jar $DEFAULT_JAVA_OPTS $JAVA_OPTS  $JAVA_PROXY_OPTS -Dhttp.nonProxyHosts="localhost|127.0.0.1|10.254.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no" /app/root.war
