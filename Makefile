.PHONY: help build test run docker-build docker-up docker-down clean install

# Variables
APP_NAME=user-management-service
VERSION=1.0.0
DOCKER_IMAGE=$(APP_NAME):$(VERSION)
DOCKER_COMPOSE=docker-compose

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-20s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

install: ## Install dependencies
	@echo "Installing dependencies..."
	mvn clean install -DskipTests

build: ## Build the application
	@echo "Building application..."
	mvn clean package -DskipTests

build-with-tests: ## Build the application with tests
	@echo "Building application with tests..."
	mvn clean package

test: ## Run tests
	@echo "Running tests..."
	mvn test

integration-test: ## Run integration tests
	@echo "Running integration tests..."
	mvn verify

run: ## Run the application locally
	@echo "Running application..."
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

run-prod: ## Run the application in production mode
	@echo "Running application in production mode..."
	java -jar target/$(APP_NAME)-$(VERSION).jar --spring.profiles.active=prod

docker-build: ## Build Docker image
	@echo "Building Docker image..."
	docker build -t $(DOCKER_IMAGE) .

docker-up: ## Start all services with docker-compose
	@echo "Starting services..."
	$(DOCKER_COMPOSE) up -d

docker-down: ## Stop all services
	@echo "Stopping services..."
	$(DOCKER_COMPOSE) down

docker-logs: ## View docker-compose logs
	@echo "Viewing logs..."
	$(DOCKER_COMPOSE) logs -f

docker-restart: ## Restart all services
	@echo "Restarting services..."
	$(DOCKER_COMPOSE) restart

docker-clean: ## Clean up docker resources
	@echo "Cleaning up Docker resources..."
	$(DOCKER_COMPOSE) down -v
	docker rmi $(DOCKER_IMAGE) || true

db-migrate: ## Run database migrations
	@echo "Running database migrations..."
	mvn flyway:migrate

db-clean: ## Clean database
	@echo "Cleaning database..."
	mvn flyway:clean

db-info: ## Show migration info
	@echo "Showing migration info..."
	mvn flyway:info

clean: ## Clean build artifacts
	@echo "Cleaning..."
	mvn clean
	rm -rf logs/

format: ## Format code
	@echo "Formatting code..."
	mvn spotless:apply || echo "Spotless not configured"

lint: ## Lint code
	@echo "Linting code..."
	mvn checkstyle:check || echo "Checkstyle not configured"

coverage: ## Generate test coverage report
	@echo "Generating coverage report..."
	mvn jacoco:report
	@echo "Coverage report available at: target/site/jacoco/index.html"

swagger: ## Open Swagger UI in browser
	@echo "Opening Swagger UI..."
	@command -v xdg-open > /dev/null && xdg-open http://localhost:8080/swagger-ui.html || open http://localhost:8080/swagger-ui.html

health-check: ## Check application health
	@echo "Checking application health..."
	@curl -s http://localhost:8080/actuator/health | jq '.' || curl -s http://localhost:8080/actuator/health

metrics: ## View application metrics
	@echo "Fetching metrics..."
	@curl -s http://localhost:8080/actuator/metrics | jq '.' || curl -s http://localhost:8080/actuator/metrics

dev-setup: ## Setup development environment
	@echo "Setting up development environment..."
	@cp .env.example .env
	@echo "Please edit .env file with your configuration"
	$(DOCKER_COMPOSE) up -d postgres redis zookeeper kafka
	@echo "Waiting for services to start..."
	@sleep 10
	@echo "Development environment ready!"

dev-teardown: ## Teardown development environment
	@echo "Tearing down development environment..."
	$(DOCKER_COMPOSE) down -v

# Database helpers
db-shell: ## Connect to database shell
	@echo "Connecting to database..."
	docker exec -it user-management-postgres psql -U postgres -d user_management

redis-cli: ## Connect to Redis CLI
	@echo "Connecting to Redis..."
	docker exec -it user-management-redis redis-cli

# Kafka helpers
kafka-topics: ## List Kafka topics
	@echo "Listing Kafka topics..."
	docker exec user-management-kafka kafka-topics --bootstrap-server localhost:9092 --list

kafka-consume: ## Consume messages from user-events topic
	@echo "Consuming from user-events topic..."
	docker exec user-management-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events --from-beginning

# Quick start commands
quickstart: dev-setup build run ## Quick start for development

all: clean install build test docker-build ## Run all build steps

.DEFAULT_GOAL := help
