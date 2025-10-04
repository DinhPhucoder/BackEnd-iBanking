FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper và pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8081

# Run application
CMD ["java", "-jar", "target/UserService-0.0.1-SNAPSHOT.jar"]
