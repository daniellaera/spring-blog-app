FROM maven:3.9.9-eclipse-temurin-21-alpine as maven-builder

COPY src /app/src

COPY pom.xml /app

RUN mvn -f /app/pom.xml clean package -DskipTests

FROM openjdk:21-jdk-slim

COPY --from=maven-builder app/target/blog-app-0.0.1-SNAPSHOT.jar /app-service/blog-app-0.0.1-SNAPSHOT.jar

WORKDIR /app-service

EXPOSE 8080

ENTRYPOINT ["java","-jar","blog-app-0.0.1-SNAPSHOT.jar"]