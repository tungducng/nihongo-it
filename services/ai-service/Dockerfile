FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy pre-built JAR file
COPY build/libs/*.jar app.jar

# Set environment variables if needed
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 