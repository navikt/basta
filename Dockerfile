FROM docker.adeo.no:5000/openjdk:8-jdk-alpine as builder
MAINTAINER Sten Røkke <sten.ivar.rokke@gmail.com>
# Builder image for WAR
# Install Maven
ARG maven_version=3.5.0
ARG user_home_dir="/root"
ARG version

ENV NO_PROXY "localhost,127.0.0.1,.local,.adeo.no,.nav.no,.aetat.no,.devillo.no,.oera.no"

RUN mkdir -p /usr/share && \
wget http://maven.adeo.no/nexus/service/local/repositories/central/content/org/apache/maven/apache-maven/$maven_version/apache-maven-$maven_version-bin.zip \
&& unzip apache-maven-$maven_version-bin.zip -d /usr/share \
&& ln -s /usr/share/apache-maven-$maven_version/bin/mvn /usr/bin/mvn

WORKDIR .
ENV MAVEN_HOME /usr/share/apache-maven-$maven_version
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
ENV MAVEN_OPTS="-XX:+TieredCompilation"
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk

RUN mkdir -p ./dist

#TODO Include settings somewhere else
COPY settings.xml /usr/share/apache-maven-$maven_version/conf/
COPY pom.xml ./dist/pom.xml
COPY src/main ./dist/src/main
COPY app-config.yaml ./dist/

WORKDIR ./dist

RUN mvn versions:set -DnewVersion=$version -DgenerateBackupPoms=false -B -q
RUN mvn clean install -DskipTests -q

FROM docker.adeo.no:5000/jetty:9.4.6-jre8-alpine

ARG version
ARG app_name

WORKDIR .

ENV JETTY_HOME /usr/local/jetty
ENV PATH $JETTY_HOME/bin:$PATH
ENV JETTY_BASE /var/lib/jetty
ENV TMPDIR /tmp/jetty
ENV APP_HOME /app/"$app_name"
ENV APP_CFG_HOME $APP_HOME/configuration

EXPOSE 8080

RUN addgroup -S srvappserver && adduser -D -S -H -G srvappserver srvappserver && rm -rf /etc/group- /etc/passwd- /etc/shadow-
RUN mkdir -p "$JETTY_HOME" && mkdir -p "$JETTY_BASE/webapps" && mkdir -p "$APP_CFG_HOME"

COPY --from=builder dist/target/$app_name-$version.war "$JETTY_BASE/webapps/root.war"

WORKDIR $JETTY_BASE

RUN set -xe \
&& java -jar "$JETTY_HOME/start.jar" --create-startd \
&& chown -R srvappserver:srvappserver "$JETTY_BASE" \
&& rm -rf /tmp/hsperfdata_root

RUN set -xe \
&& mkdir -p "$TMPDIR" \
&& chmod 777 "$TMPDIR" \
&& chown -R srvappserver:srvappserver "$TMPDIR"

CMD java -jar "$JETTY_HOME/start.jar" -Dapp.home="$APP_HOME" -Djava.security.egd=file:/dev/./urandom

HEALTHCHECK --interval=5m --timeout=3s CMD curl -f http://localhost:8080/ || exit 1
