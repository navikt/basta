FROM docker.adeo.no:5000/openjdk:8-jre-alpine

ARG version
ARG app_name

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"
ENV DEFAULT_JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENV JAVA_OPTS="$JAVA_OPTS -Dspring.flyway.enabled=true"

COPY target/$app_name-$version.war "/app/root.war"

WORKDIR /app

EXPOSE 8080

ENV JAVA_PROXY_OPTS="-Dhttp.proxyHost=webproxy.nais -Dhttps.proxyHost=webproxy.nais -Dhttp.proxyPort=8088 -Dhttps.proxyPort=8088"

CMD java -jar $DEFAULT_JAVA_OPTS $JAVA_OPTS  $JAVA_PROXY_OPTS -Dhttp.nonProxyHosts="localhost|127.0.0.1|10.254.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no" /app/root.war



#FROM navikt/java:8



#ENV JAVA_OPTS="$JAVA_OPTS -Dspring.flyway.enabled=true"
#COPY init-scripts /init-scripts
#COPY nais/run-java.sh /run-java.sh
#RUN chmod +x /run-java.sh
#RUN groupadd -r basta && useradd -r -s /bin/false -g basta basta
#COPY kuhr-fbv-faktura-app/target/lib /app/lib/
#COPY kuhr-fbv-faktura-app/target/*.jar /app/
#RUN chown -R basta:basta /app
#USER basta


#COPY target/*.war "/app/root.war"
#ENV JAVA_PROXY_OPTS="-Dhttp.proxyHost=webproxy.nais -Dhttps.proxyHost=webproxy.nais -Dhttp.proxyPort=8088 -Dhttps.proxyPort=8088"

#CMD java -jar $DEFAULT_JAVA_OPTS $JAVA_OPTS  $JAVA_PROXY_OPTS -Dhttp.nonProxyHosts="localhost|127.0.0.1|10.254.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no" /app/root.war
