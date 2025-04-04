# Build stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/food-ordering-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]