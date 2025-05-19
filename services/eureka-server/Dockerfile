FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the entire current directory
COPY . .

# Give execution permission to gradlew
RUN chmod +x ./gradlew

# Build the JAR file
RUN ./gradlew clean build -x test

# Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Set environment variables if needed
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 