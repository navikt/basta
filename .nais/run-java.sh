export JAVA_OPTS="$JAVA_OPTS -Dspring.flyway.enabled=true"

echo "java $DEFAULT_JVM_OPTS $DEFAULT_JAVA_OPTS $JAVA_OPTS -jar /app/root.war"
exec java $DEFAULT_JVM_OPTS $DEFAULT_JAVA_OPTS $JAVA_OPTS -jar /app/root.war
