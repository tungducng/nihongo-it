#!/bin/bash

# Nihongo-IT - Service Stop Script
# This script stops running services of the Nihongo-IT application

# Define colors for better readability
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get absolute path to project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}=== Nihongo-IT - Service Terminator ===${NC}"

# Check if PIDs file exists
if [ -f "$PROJECT_ROOT/logs/pids.txt" ]; then
    echo -e "${YELLOW}Found running services. Stopping them now...${NC}"
    
    # Read PIDs from file
    PIDS=$(cat "$PROJECT_ROOT/logs/pids.txt")
    
    # Kill all processes
    for PID in $PIDS; do
        if ps -p $PID > /dev/null; then
            echo -e "${GREEN}Stopping process with PID: $PID${NC}"
            kill $PID
        else
            echo -e "${YELLOW}Process with PID: $PID is not running${NC}"
        fi
    done
    
    # Remove PIDs file
    rm "$PROJECT_ROOT/logs/pids.txt"
    echo -e "${GREEN}All services stopped successfully${NC}"
else
    # Try to find and kill services by port
    echo -e "${YELLOW}PIDs file not found. Trying to find services by port...${NC}"
    
    # Kill Spring Boot service on port 8080
    SPRING_PID=$(lsof -t -i:8080)
    if [ ! -z "$SPRING_PID" ]; then
        echo -e "${GREEN}Stopping Spring Boot service on port 8080 (PID: $SPRING_PID)${NC}"
        kill $SPRING_PID
    else
        echo -e "${YELLOW}No service found on port 8080${NC}"
    fi
    
    # Kill Python service on port 8000
    PYTHON_PID=$(lsof -t -i:8000)
    if [ ! -z "$PYTHON_PID" ]; then
        echo -e "${GREEN}Stopping Python service on port 8000 (PID: $PYTHON_PID)${NC}"
        kill $PYTHON_PID
    else
        echo -e "${YELLOW}No service found on port 8000${NC}"
    fi
    
    # Kill Vue.js service on port 5173
    VUE_PID=$(lsof -t -i:5173)
    if [ ! -z "$VUE_PID" ]; then
        echo -e "${GREEN}Stopping Vue.js service on port 5173 (PID: $VUE_PID)${NC}"
        kill $VUE_PID
    else
        echo -e "${YELLOW}No service found on port 5173${NC}"
    fi
    
    echo -e "${GREEN}Service termination completed${NC}"
fi

echo -e "${BLUE}=== Nihongo-IT Services Stopped ===${NC}" 