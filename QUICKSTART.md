# Quick Start Guide

Get the User Management Service running in under 5 minutes!

## Prerequisites

- Docker and Docker Compose installed
- (Optional) Java 17+ and Maven for local development

## Option 1: Docker Compose (Recommended)

### 1. Start All Services

```bash
# Clone the repository
git clone <repository-url>
cd user-management-service

# Copy environment variables
cp .env.example .env

# Start all services (PostgreSQL, Redis, Kafka, Application)
docker-compose up -d
```

### 2. Wait for Services to Start

```bash
# Check service status
docker-compose ps

# View application logs
docker-compose logs -f app
```

The application will be ready when you see:
```
Started UserManagementServiceApplication in X seconds
```

### 3. Access the Application

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### 4. Test the API

Register a new user:

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

Login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "TestPass123!"
  }'
```

### 5. Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Option 2: Local Development

### 1. Start Dependencies

```bash
# Start PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=user_management \
  -p 5432:5432 \
  postgres:16-alpine

# Start Redis
docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine

# Start Zookeeper
docker run -d --name zookeeper \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -p 2181:2181 \
  confluentinc/cp-zookeeper:7.5.0

# Start Kafka
docker run -d --name kafka \
  -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -p 9092:9092 \
  confluentinc/cp-kafka:7.5.0
```

### 2. Build and Run the Application

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/user-management-service-1.0.0.jar

# Or run with Maven
mvn spring-boot:run
```

### 3. Verify Installation

```bash
# Check health
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP","components":{...}}
```

## Common Commands

### Docker Compose

```bash
# View logs
docker-compose logs -f [service-name]

# Restart a service
docker-compose restart [service-name]

# Scale the application
docker-compose up -d --scale app=3

# Execute commands in container
docker-compose exec app sh

# View resource usage
docker-compose stats
```

### Maven

```bash
# Run tests
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest

# Skip tests
mvn clean package -DskipTests

# Generate code coverage report
mvn jacoco:report

# Check for dependency updates
mvn versions:display-dependency-updates
```

## Development Workflow

### 1. Make Code Changes

Edit source files in `src/main/java/com/usermanagement/`

### 2. Run Tests

```bash
mvn test
```

### 3. Build Application

```bash
mvn clean package
```

### 4. Rebuild Docker Image

```bash
docker-compose build app
docker-compose up -d app
```

## Useful API Endpoints

### Public Endpoints (No Authentication)

```bash
# Register user
POST /api/v1/users/register

# Login
POST /api/v1/auth/login

# Refresh token
POST /api/v1/auth/refresh

# Health check
GET /api/v1/health
GET /actuator/health
```

### Protected Endpoints (Require Authentication)

```bash
# Get user by ID
GET /api/v1/users/{id}
Header: Authorization: Bearer {token}

# Update user
PUT /api/v1/users/{id}
Header: Authorization: Bearer {token}

# Get all users (Admin only)
GET /api/v1/users
Header: Authorization: Bearer {token}

# Delete user (Admin only)
DELETE /api/v1/users/{id}
Header: Authorization: Bearer {token}
```

## Accessing Services

### PostgreSQL

```bash
# Connect to PostgreSQL
docker exec -it user-management-postgres psql -U postgres -d user_management

# List tables
\dt

# View users
SELECT * FROM users;

# Exit
\q
```

### Redis

```bash
# Connect to Redis
docker exec -it user-management-redis redis-cli

# List all keys
KEYS *

# Get a value
GET user:{uuid}

# Exit
exit
```

### Kafka

```bash
# List topics
docker exec -it user-management-kafka \
  kafka-topics --list --bootstrap-server localhost:9092

# Consume messages
docker exec -it user-management-kafka \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

## Troubleshooting

### Port Already in Use

If you see "port already in use" errors:

```bash
# Check what's using the port
lsof -i :8080  # or :5432, :6379, :9092

# Kill the process
kill -9 <PID>
```

### Cannot Connect to Database

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# View PostgreSQL logs
docker logs user-management-postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Application Won't Start

```bash
# View application logs
docker-compose logs app

# Common issues:
# 1. Database not ready - wait 10-20 seconds and retry
# 2. Port in use - change SERVER_PORT in .env
# 3. Invalid JWT secret - ensure JWT_SECRET is at least 256 bits
```

### Clear All Data

```bash
# Stop and remove all containers and volumes
docker-compose down -v

# Restart fresh
docker-compose up -d
```

## Environment Variables

Key environment variables in `.env`:

```properties
# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=user_management
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT (IMPORTANT: Change in production!)
JWT_SECRET=your-256-bit-secret-key-change-this-in-production
JWT_EXPIRATION_MS=86400000           # 24 hours
JWT_REFRESH_EXPIRATION_MS=604800000  # 7 days

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_TTL_SECONDS=3600

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_TOPIC_USER_EVENTS=user-events
```

## Next Steps

1. **Explore the API**: Open http://localhost:8080/swagger-ui.html
2. **Read the Documentation**: Check out README.md and ARCHITECTURE.md
3. **Review Examples**: See API_EXAMPLES.md for detailed API usage
4. **Run Tests**: Execute `mvn test` to run the test suite
5. **Customize Configuration**: Edit application.yml for your needs

## Production Checklist

Before deploying to production:

- [ ] Change JWT_SECRET to a strong, random value
- [ ] Use strong database credentials
- [ ] Configure Redis authentication
- [ ] Set up OAuth2 providers
- [ ] Enable HTTPS/TLS
- [ ] Configure firewall rules
- [ ] Set up monitoring and alerting
- [ ] Configure backup strategy
- [ ] Review security settings
- [ ] Set SPRING_PROFILES_ACTIVE=prod

## Getting Help

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Architecture**: See ARCHITECTURE.md
- **API Examples**: See API_EXAMPLES.md
- **Issues**: Open an issue on GitHub

## License

Apache License 2.0
