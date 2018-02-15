if [ "$#" -lt 1 ]; then
  echo "Please add version to build"
  exit 1
fi

VERSION=$1
mvn clean && npm install && gulp dist && mvn install -DskipTests
docker build --build-arg version=$VERSION-SNAPSHOT --build-arg app_name=basta -t docker.adeo.no:5000/basta:$VERSION .
docker run -p 8080:8080 --env-file src/test/resources/bastaDocker.properties docker.adeo.no:5000/basta:$VERSION
