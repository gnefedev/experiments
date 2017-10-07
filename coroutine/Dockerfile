FROM openjdk:8-jdk
ADD coroutine-1.0.jar app.jar
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar