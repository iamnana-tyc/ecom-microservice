# Use stable base image
FROM eclipse-temurin:21-jdk-ubi9-minimal

# App directory
WORKDIR /app

# Copy jar
COPY target/*.jar app.jar

#PORT
EXPOSE 8761

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]