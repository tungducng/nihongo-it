# Nihongo IT - Microservice Architecture

This project is a Japanese learning platform built using a microservice architecture. Each service is responsible for a specific domain of the application.

## Architecture Overview

The application is composed of the following microservices:

1. **API Gateway (`api-gateway`)**: Acts as an entry point for all client requests, handles routing to appropriate services, and implements cross-cutting concerns like security, logging, and rate limiting.

2. **User Service (`user-service`)**: Manages user authentication, authorization, profiles, and user-related operations.

3. **Learning Service (`learning-service`)**: Core service responsible for managing learning resources, courses, lessons, exercises, and user progress tracking.

4. **AI Service (`ai-service`)**: Handles AI-powered features such as text-to-speech, speech analysis, chat interactions with AI assistants, and other AI functionalities.

5. **Utility Service (`utility-service`)**: Provides utility functions like Furigana generation, Japanese language processing, and other helper functions.

6. **Notification Service (`notification-service`)**: Manages email notifications, alerts, and other communication channels.

7. **Statistic Service (`statistic-service`)**: Collects and analyzes user activities and learning patterns, generating reports and insights.

## Service Communication

Services communicate with each other using:
- Synchronous communication via REST APIs (with Feign clients)
- Service discovery through Netflix Eureka
- Circuit breaking patterns for resilience

## Technology Stack

- **Programming Language**: Kotlin
- **Framework**: Spring Boot 3.x
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Circuit Breaker**: Resilience4j
- **Database**: PostgreSQL
- **Build Tool**: Gradle with Kotlin DSL
- **Authentication**: JWT + OAuth2
- **Monitoring**: Spring Boot Actuator

## Directory Structure

Each microservice follows a similar structure:

```
service-name/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/servicename/
│   │   │   ├── config/       # Configuration classes
│   │   │   ├── controller/   # REST controllers
│   │   │   ├── service/      # Business logic
│   │   │   ├── repository/   # Data access
│   │   │   ├── entity/       # Domain models
│   │   │   ├── dto/          # Data transfer objects
│   │   │   ├── exception/    # Custom exceptions
│   │   │   └── util/         # Utility classes
│   │   └── resources/
│   │       ├── application.yml  # Service configuration
│   │       └── db/migration/    # Flyway migrations
│   └── test/                    # Test code
└── build.gradle.kts             # Gradle build file
```

## Setup and Deployment

### Prerequisites
- JDK 21
- Docker and Docker Compose
- PostgreSQL

### Development Environment Setup

1. Clone the repository
```bash
git clone https://github.com/tungducng/nihongo-it.git
cd nihongo-it
```

2. Set up environment variables or modify the default values in application.yml files

3. Start the services using Docker Compose
```bash
# Start Docker containers for PostgreSQL and other dependencies
./start-docker.sh

# Start service discovery (Eureka server)
./gradlew :eureka-server:bootRun

# Start other services
./gradlew :api-gateway:bootRun
./gradlew :user-service:bootRun
./gradlew :learning-service:bootRun
./gradlew :ai-service:bootRun
./gradlew :notification-service:bootRun
```

Alternatively, you can start all services at once:
```bash
./start-all-services.sh
```

### API Documentation

Each service exposes its API documentation via Swagger UI:

- API Gateway: http://localhost:8080/api-docs
- User Service: http://localhost:8086/swagger-ui.html
- Learning Service: http://localhost:8081/swagger-ui.html
- AI Service: http://localhost:8082/swagger-ui.html
- Utility Service: http://localhost:8083/swagger-ui.htm
- Notification Service: http://localhost:8084/swagger-ui.html

## Frontend Application

The frontend application is a separate repository located in the `nihongo-it-frontend` directory.

## License

[MIT License](LICENSE) 