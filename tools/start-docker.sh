#!/bin/bash

# Nihongo-IT - Docker Service Starter
# This script starts all Docker services defined in docker-compose.yaml

# Define colors for better readability
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get absolute path to project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${BLUE}=== Nihongo-IT - Docker Service Starter ===${NC}"
echo -e "${YELLOW}Project root: $PROJECT_ROOT${NC}"

# Check if docker-compose.yaml exists
if [ ! -f "$PROJECT_ROOT/docker/docker-compose.yaml" ]; then
    echo -e "${RED}ERROR: docker/docker-compose.yaml not found${NC}"
    exit 1
fi

# Start Docker services
echo -e "${GREEN}Starting Docker services...${NC}"
cd "$PROJECT_ROOT/docker"
docker-compose up -d

echo -e "${GREEN}Docker services started successfully.${NC}"
echo -e "${YELLOW}To check service status, run: docker-compose ps${NC}"
echo -e "${RED}To stop all services, run: docker-compose down${NC}" 