# Nihongo-IT Service Scripts

This document explains how to use the service scripts to start and stop the Nihongo-IT application components.

## Prerequisites

Make sure you have the following installed:
- Java (for Spring Boot backend)
- Node.js and npm (for Vue.js frontend)
- Python 3 with venv support (for Python FastAPI service)
- lsof command (used to check for available ports)

## Available Scripts

### Starting All Services

To start all services (Python FastAPI, Spring Boot, and Vue.js frontend), run:

```bash
./start-all-services.sh
```

This script will:
1. Check if all required directories exist
2. Verify that the necessary ports (8000, 8080, and 5173) are available
3. Start the Python FastAPI service on port 8000
4. Start the Spring Boot service on port 8080
5. Start the Vue.js frontend on port 5173
6. Create log files in the `logs` directory

When running for the first time, the script will:
- Create a Python virtual environment and install dependencies
- Prompt for an OpenAI API key if not already configured
- Install npm dependencies for the frontend

### Stopping All Services

To stop all running services, you can either:

1. Press `Ctrl+C` in the terminal where the start script is running, or
2. Run the stop script:

```bash
./stop-services.sh
```

The stop script will terminate all services either by using the saved PIDs or by finding services running on the expected ports.

## Service URLs

Once the services are running, you can access them at:

- Python FastAPI: http://localhost:8000
- Spring Boot API: http://localhost:8080
- Vue.js Frontend: http://localhost:5173

## Logs

Log files for each service are stored in the `logs` directory:
- Python service: `logs/python.log`
- Spring Boot service: `logs/java.log`
- Vue.js service: `logs/vue.log`

## Troubleshooting

If any service fails to start:
1. Check the corresponding log file in the `logs` directory
2. Ensure all required dependencies are installed
3. Verify that the ports are not being used by other applications

For the Python service, make sure your OpenAI API key is valid if you're using features that require it. 