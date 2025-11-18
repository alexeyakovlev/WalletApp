FROM openjdk:17-ea-3-jdk-slim

WORKDIR /app

COPY target/WalletApp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]