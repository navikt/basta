export JAVA_OPTS="$JAVA_OPTS -Dspring.flyway.enabled=true --add-opens java.base/java.time=ALL-UNNAMED"

echo "java $DEFAULT_JVM_OPTS $DEFAULT_JAVA_OPTS $JAVA_OPTS -jar /app/root.war"
exec java $DEFAULT_JVM_OPTS $DEFAULT_JAVA_OPTS $JAVA_OPTS -jar /app/root.war
