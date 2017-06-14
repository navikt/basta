FROM docker.adeo.no:5000/openjdk:8-jdk-alpine
# Builder image for WAR
# Install Maven
ARG maven_version=3.5.0
ARG user_home_dir="/root"
ARG version
ARG app_name

ENV NO_PROXY "localhost,127.0.0.1,.local,.adeo.no,.nav.no,.aetat.no,.devillo.no,.oera.no"

RUN mkdir -p /usr/share && \
wget http://maven.adeo.no/nexus/service/local/repositories/central/content/org/apache/maven/apache-maven/$maven_version/apache-maven-$maven_version-bin.zip \
&& unzip apache-maven-$maven_version-bin.zip -d /usr/share \
&& ln -s /usr/share/apache-maven-$maven_version/bin/mvn /usr/bin/mvn

WORKDIR .
ENV MAVEN_HOME /usr/share/apache-maven-$maven_version
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk

RUN mkdir -p ./dist

#TODO Include settings somewhere else
COPY settings.xml /usr/share/apache-maven-$maven_version/conf/
COPY pom.xml ./dist/pom.xml
COPY war/ ./dist/war/
#COPY ear/ ./dist/ear/
COPY appconfig/ ./dist/appconfig/
COPY Dockerfile.run ./dist/Dockerfile
COPY environment.properties ./dist/environment.properties

WORKDIR ./dist

#RUN mvn -X -T 1C install && rm -rf target
RUN mvn versions:set -DnewVersion=$version -DgenerateBackupPoms=false -B
RUN mvn clean install -DskipTests && mv war/target/*.war . && rm -rf dist/war dist/appconfig dist/pom.xml

CMD tar -cf - .
