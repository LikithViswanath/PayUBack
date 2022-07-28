FROM openjdk:17
WORKDIR /app

ADD ./target/auth-service-0.0.1-SNAPSHOT.jar auth-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "auth-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 9001