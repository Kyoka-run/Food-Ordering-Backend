# Build stage
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml for dependency resolution
COPY pom.xml .
# Download dependencies to cache in a separate layer
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Environment variables for database connection
ENV DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=kyoka_food_order \
    DB_USERNAME=root \
    DB_PASSWORD=Cinder1014 \
    AWS_S3_BUCKET="" \
    AWS_REGION="" \
    SERVER_PORT=8080

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]