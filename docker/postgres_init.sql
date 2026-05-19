-- Database-per-service initialization
-- Runs once on first Postgres container start (mounted into /docker-entrypoint-initdb.d/).
-- Note: POSTGRES_DB env var still creates a default DB; we add the per-service DBs here.

CREATE DATABASE user_service;
CREATE DATABASE learning_service;
CREATE DATABASE notification_service;

-- ai-service, eureka-server, api-gateway have no persistence layer.
