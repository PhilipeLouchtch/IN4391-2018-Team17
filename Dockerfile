FROM openjdk:8-jre-alpine

COPY *.jar app.jar

EXPOSE 80

ENTRYPOINT ["java"]
CMD ["-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom", "-jar", "./app.jar"]