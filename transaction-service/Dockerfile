FROM openjdk:17
WORKDIR /app

ADD ./target/transaction-service-0.0.1-SNAPSHOT.jar transaction-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "transaction-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 9003