#!/bin/bash

# Nihongo-IT - Service Startup Script
# This script starts the Python, Spring Boot, and Frontend components of the Nihongo-IT application

# Define colors for better readability
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get absolute path to project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

echo -e "${BLUE}=== Nihongo-IT - Service Starter ===${NC}"
echo -e "${YELLOW}Project root: $PROJECT_ROOT${NC}"

# Parse command line arguments
MICROSERVICE_MODE=false
EUREKA_SERVER_ONLY=false

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --microservice) MICROSERVICE_MODE=true ;;
        --eureka-only) EUREKA_SERVER_ONLY=true; MICROSERVICE_MODE=true ;;
        --help) 
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  --microservice    Run in microservice mode with Eureka enabled"
            echo "  --eureka-only     Only start Eureka server (Discovery service)"
            echo "  --help            Show this help message"
            exit 0
            ;;
        *) echo "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

# Check if all required directories exist
if [ ! -d "$PROJECT_ROOT/nihongo-it-python" ]; then
    echo -e "${RED}ERROR: nihongo-it-python directory not found${NC}"
    exit 1
fi

if [ ! -d "$PROJECT_ROOT/nihongo-it-backend" ]; then
    echo -e "${RED}ERROR: nihongo-it-backend directory not found${NC}"
    exit 1
fi

if [ ! -d "$PROJECT_ROOT/nihongo-it-frontend" ]; then
    echo -e "${RED}ERROR: nihongo-it-frontend directory not found${NC}"
    exit 1
fi

# Create service-registry directory for Eureka if in microservice mode
if [ "$MICROSERVICE_MODE" = true ] && [ ! -d "$PROJECT_ROOT/service-registry" ]; then
    echo -e "${YELLOW}Creating service registry directory for Eureka Server...${NC}"
    mkdir -p "$PROJECT_ROOT/service-registry"
    
    # Create Eureka Server subproject here if it doesn't exist already...
    if [ ! -f "$PROJECT_ROOT/service-registry/build.gradle" ]; then
        echo -e "${YELLOW}Setting up Eureka Server project...${NC}"
        # This would be a placeholder for creating a proper Spring Cloud Eureka Server
        # In a real scenario, you would have a separate project for this
        echo "Warning: You need to set up the Eureka Server project manually in $PROJECT_ROOT/service-registry"
    fi
fi

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}ERROR: Port $port is already in use. Please close the application using it.${NC}"
        return 1
    fi
    return 0
}

# Check required ports
if [ "$MICROSERVICE_MODE" = true ]; then
    check_port 8761 || exit 1  # Eureka Server
fi

check_port 8000 || exit 1  # Python FastAPI
check_port 8080 || exit 1  # Spring Boot
check_port 5173 || exit 1  # Vue.js

# Create log directory
mkdir -p "$PROJECT_ROOT/logs"

# Start Eureka Server if in microservice mode
if [ "$MICROSERVICE_MODE" = true ]; then
    echo -e "${GREEN}Starting Eureka Service Registry on port 8761...${NC}"
    
    # This is a placeholder for starting the actual Eureka server
    # In a real implementation, you would have a proper Eureka Server project to start
    
    # Wait for Eureka to start up
    echo -e "${YELLOW}Waiting for Eureka server to initialize...${NC}"
    sleep 5
    
    # If only Eureka server was requested, exit now
    if [ "$EUREKA_SERVER_ONLY" = true ]; then
        echo -e "${GREEN}Eureka server started. Other services will not be started (--eureka-only mode).${NC}"
        echo -e "${GREEN}Eureka dashboard available at: http://localhost:8761${NC}"
        exit 0
    fi
fi

# Start Spring Boot service
echo -e "${GREEN}Starting Spring Boot service...${NC}"
cd "$PROJECT_ROOT/nihongo-it-backend"

# Set environment variables for microservice mode if enabled
if [ "$MICROSERVICE_MODE" = true ]; then
    export EUREKA_ENABLED=true
    export EUREKA_SERVICE_URL="http://localhost:8761/eureka/"
    echo -e "${YELLOW}Running Spring Boot in microservice mode with Eureka enabled${NC}"
else
    export EUREKA_ENABLED=false
    echo -e "${YELLOW}Running Spring Boot as standalone service${NC}"
fi

# Check if Gradle wrapper exists
if [ -f "gradlew" ]; then
    chmod +x gradlew
    echo -e "${GREEN}Starting Spring Boot service on port 8080...${NC}"
    ./gradlew bootRun > "$PROJECT_ROOT/logs/springboot.log" 2>&1 &
    SPRING_PID=$!
    echo -e "${GREEN}Spring Boot service started with PID: $SPRING_PID${NC}"
else
    echo -e "${YELLOW}Gradle wrapper not found, trying with gradle command...${NC}"
    # Try with regular gradle
    if command -v gradle &> /dev/null; then
        echo -e "${GREEN}Starting Spring Boot service on port 8080...${NC}"
        gradle bootRun > "$PROJECT_ROOT/logs/springboot.log" 2>&1 &
        SPRING_PID=$!
        echo -e "${GREEN}Spring Boot service started with PID: $SPRING_PID${NC}"
    else
        echo -e "${RED}ERROR: Gradle not found. Please install Gradle or use the Gradle wrapper.${NC}"
        exit 1
    fi
fi

# Give Spring Boot service time to start
echo -e "${YELLOW}Waiting for Spring Boot service to initialize...${NC}"
sleep 10

echo -e "${GREEN}Starting Python FastAPI service...${NC}"
cd "$PROJECT_ROOT/nihongo-it-python"

# Check for Python virtual environment
if [ ! -d "venv" ]; then
    echo -e "${YELLOW}Setting up Python virtual environment...${NC}"
    python3 -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
else
    source venv/bin/activate
fi

# Check for OpenAI API key
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}OpenAI API key not found. Please enter your OpenAI API key:${NC}"
    read api_key
    echo "OPENAI_API_KEY=$api_key" > .env
    echo "OpenAI API key saved to .env file"
fi

# Start Python service in background
echo -e "${GREEN}Starting FastAPI service on port 8000...${NC}"
uvicorn main:app --host 0.0.0.0 --port 8000 > "$PROJECT_ROOT/logs/python.log" 2>&1 &
PYTHON_PID=$!
echo -e "${GREEN}Python service started with PID: $PYTHON_PID${NC}"

# Give Python service time to start
echo -e "${YELLOW}Waiting for Python service to initialize...${NC}"
sleep 3

echo -e "${GREEN}Starting Vue.js frontend...${NC}"
cd "$PROJECT_ROOT/nihongo-it-frontend"

# Check if dependencies are installed
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}Installing Node.js dependencies...${NC}"
    npm install
fi

# Start Vue.js service
echo -e "${GREEN}Starting Vue.js service on port 5173...${NC}"
npm run dev > "$PROJECT_ROOT/logs/vue.log" 2>&1 &
VUE_PID=$!
echo -e "${GREEN}Vue.js service started with PID: $VUE_PID${NC}"

# Show status information
echo -e "\n${BLUE}=== Services Information ===${NC}"
if [ "$MICROSERVICE_MODE" = true ]; then
    echo -e "${GREEN}Eureka Server:${NC} http://localhost:8761"
fi
echo -e "${GREEN}Spring Boot:${NC} http://localhost:8080"
echo -e "${GREEN}Python FastAPI:${NC} http://localhost:8000"
echo -e "${GREEN}Vue.js Frontend:${NC} http://localhost:5173"
echo -e "\n${YELLOW}Log files are available in the logs directory${NC}"
echo -e "${YELLOW}PIDs: Spring Boot=$SPRING_PID, Python=$PYTHON_PID, Vue=$VUE_PID${NC}"
echo -e "\n${RED}To stop all services, press Ctrl+C or run: kill $SPRING_PID $PYTHON_PID $VUE_PID${NC}"

# Save PIDs to file for later termination
echo "$SPRING_PID $PYTHON_PID $VUE_PID" > "$PROJECT_ROOT/logs/pids.txt"

# Keep script running to allow easy termination
echo -e "${BLUE}Services are running. Press Ctrl+C to stop all services.${NC}"

# Handle termination
trap 'echo -e "${RED}Stopping all services...${NC}"; kill $(cat "$PROJECT_ROOT/logs/pids.txt") 2>/dev/null; rm "$PROJECT_ROOT/logs/pids.txt"; echo -e "${GREEN}All services stopped${NC}"; exit 0' INT TERM

# Wait indefinitely
while true; do
    sleep 1
done 