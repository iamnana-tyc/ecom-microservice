# Use stable base image
FROM eclipse-temurin:21-jre

# App directory
WORKDIR /app

# Copy jar
COPY target/*.jar app.jar

#PORT
EXPOSE 8084

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]