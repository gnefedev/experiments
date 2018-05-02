FROM openjdk:8-jdk
ADD flux-1.0.jar app.jar
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar