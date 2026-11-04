FROM        maven:3.10.0-eclipse-temurin-25 AS builder
WORKDIR     /app
COPY        . .
RUN         mvn package -DskipTests

FROM        eclipse-temurin:25-jre-alpine-3.24
COPY        --from=builder /app/target/app.jar /app/app.jar
ENTRYPOINT  ["java", "-ea", "-jar", "/app/app.jar"]
