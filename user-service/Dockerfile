FROM openjdk:17
WORKDIR /app

ADD ./target/user-service-0.0.1-SNAPSHOT.jar user-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "user-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 9003