# Use a lightweight OpenJDK 17 image
FROM openjdk:17-jdk-slim

# Set environment variables
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

# Create a directory for the app
WORKDIR /app

# Copy the built jar from target/ or build/libs/ depending on Gradle config
COPY build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
