FROM openjdk:17
WORKDIR /app

ADD ./target/schedule-service-0.0.1-SNAPSHOT.jar schedule-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "schedule-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 9003