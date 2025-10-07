########## Build stage ##########
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests clean package

########## Run stage ##########
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8082
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
