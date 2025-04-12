# Kotlin API Project Setup

This README contains instructions for setting up and maintaining the project's code quality and API clients.

## API Documentation

To download the OpenAPI documentation:

```bash
curl -o openapi.json http://localhost:8080/v1/api-docs
```

This command downloads the API documentation from the local development server and saves it as `openapi.json`.

## Code Generation

To generate API client code from the OpenAPI specification:

```bash
./gradlew openApiGenerate
```

This will create client code based on the OpenAPI specification downloaded in the previous step.

## Code Quality

### Check Code Style

To check your Kotlin code for style violations:

```bash
./gradlew ktlintCheck
```

This command will report any style issues found in your Kotlin code according to the Kotlin style guide.

### Format Code

To automatically format your Kotlin code to conform to the style guidelines:

```bash
./gradlew ktlintFormat
```

This command will automatically fix most style issues in your Kotlin code.

## Quick Start

To get started quickly, run the following commands in sequence:

1. Download API documentation
2. Generate API client code
3. Format code to ensure consistency

```bash
curl -o openapi.json http://localhost:8080/v3/api-docs
./gradlew openApiGenerate
./gradlew ktlintFormat
```