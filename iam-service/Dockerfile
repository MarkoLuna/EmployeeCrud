FROM openjdk:21-jdk-slim

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Run the application
EXPOSE 8082
CMD ["java", "-jar", "target/iam-service-1.0-SNAPSHOT.jar"]
