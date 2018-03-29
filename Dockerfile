FROM openjdk:8-jre-alpine

COPY *.jar app.jar

EXPOSE 80
EXPOSE 5005

ENTRYPOINT ["java"]
CMD ["-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005", "-jar", "./app.jar"]