# User Management Service

A production-ready user management microservice built with Spring Boot, featuring JWT authentication, OAuth2 PKCE, Redis caching, and Kafka event streaming.

## Features

- **User Management**: Complete CRUD operations for user accounts
- **Authentication & Authorization**:
  - JWT-based authentication with access and refresh tokens
  - OAuth2 with PKCE support
  - Role-based access control (RBAC)
  - Permission-based authorization
- **Security**:
  - Password encryption with BCrypt
  - Account locking after failed login attempts
  - Token rotation and revocation
  - CORS configuration
- **Caching**: Redis-based distributed caching for improved performance
- **Event Streaming**: Kafka integration for event-driven architecture
- **API Documentation**: OpenAPI/Swagger UI
- **Observability**:
  - Health checks
  - Prometheus metrics
  - Structured logging
- **Database**: PostgreSQL with Flyway migrations

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.2**
- **PostgreSQL 16**
- **Redis 7**
- **Apache Kafka**
- **JWT (io.jsonwebtoken)**
- **MapStruct** for DTO mapping
- **Flyway** for database migrations
- **Docker & Docker Compose**

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker and Docker Compose (for local development)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd user-management-service
```

### 2. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and configure your environment-specific values:

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=user_management
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT Secret (change in production!)
JWT_SECRET=your-256-bit-secret-key-change-this-in-production

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### 3. Run with Docker Compose (Recommended)

Start all services (PostgreSQL, Redis, Kafka, and the application):

```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`

### 4. Run Locally (Without Docker)

#### Start Dependencies

```bash
# Start PostgreSQL
docker run -d --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=user_management -p 5432:5432 postgres:16-alpine

# Start Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Start Kafka (requires Zookeeper)
docker run -d --name zookeeper -e ZOOKEEPER_CLIENT_PORT=2181 -p 2181:2181 confluentinc/cp-zookeeper:7.5.0
docker run -d --name kafka -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -p 9092:9092 confluentinc/cp-kafka:7.5.0
```

#### Build and Run the Application

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/user-management-service-1.0.0.jar
```

Or run directly with Maven:

```bash
mvn spring-boot:run
```

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification is available at:

```
http://localhost:8080/api-docs
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | User login | No |
| POST | `/api/v1/auth/refresh` | Refresh access token | No |
| POST | `/api/v1/auth/logout` | User logout | No |

### User Management

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| POST | `/api/v1/users/register` | Register new user | No | - |
| GET | `/api/v1/users/{id}` | Get user by ID | Yes | USER, ADMIN |
| GET | `/api/v1/users` | Get all users (paginated) | Yes | ADMIN |
| PUT | `/api/v1/users/{id}` | Update user | Yes | USER, ADMIN |
| DELETE | `/api/v1/users/{id}` | Delete user | Yes | ADMIN |
| PATCH | `/api/v1/users/{id}/enable` | Enable user | Yes | ADMIN |
| PATCH | `/api/v1/users/{id}/disable` | Disable user | Yes | ADMIN |
| PATCH | `/api/v1/users/{id}/lock` | Lock user | Yes | ADMIN |
| PATCH | `/api/v1/users/{id}/unlock` | Unlock user | Yes | ADMIN |

### Health & Monitoring

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/health` | Health check | No |
| GET | `/actuator/health` | Actuator health | No |
| GET | `/actuator/metrics` | Prometheus metrics | No |

## Example Usage

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "SecurePass123!"
  }'
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "issuedAt": "2024-01-15T10:30:00",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_USER"]
  }
}
```

### 3. Access Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/v1/users/{userId} \
  -H "Authorization: Bearer {accessToken}"
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "{refreshToken}"
  }'
```

## Architecture

### Clean Architecture Layers

```
├── controller/          # REST API Layer (Presentation)
├── service/            # Business Logic Layer
│   ├── impl/          # Service implementations
├── repository/         # Data Access Layer
├── domain/            # Domain Models
│   ├── entity/        # JPA Entities
│   ├── dto/           # Data Transfer Objects
│   └── enums/         # Enumerations
├── security/          # Security components (JWT, OAuth2)
├── config/            # Configuration classes
├── exception/         # Exception handling
└── mapper/            # DTO-Entity mappers
```

### Database Schema

#### Users Table
- Stores user account information
- Includes audit fields (created_at, updated_at)
- Version field for optimistic locking

#### Roles Table
- Predefined roles (USER, ADMIN, MODERATOR, SUPER_ADMIN)
- Many-to-many relationship with users

#### Permissions Table
- Granular permissions for fine-grained access control
- Many-to-many relationship with roles

#### Refresh Tokens Table
- Stores refresh tokens with expiration
- Tracks IP address and user agent
- Supports token revocation

## Security Features

### Password Requirements

Passwords must contain:
- At least 8 characters
- One uppercase letter
- One lowercase letter
- One digit
- One special character (@$!%*?&)

### Account Locking

- User accounts are automatically locked after 5 failed login attempts
- Admins can manually unlock accounts via API

### Token Management

- **Access Token**: Short-lived (24 hours by default)
- **Refresh Token**: Long-lived (7 days by default)
- Refresh tokens can be revoked on logout
- Tokens are stored in database for validation

## Caching Strategy

Redis is used for caching user data:

- User responses are cached with configurable TTL (1 hour by default)
- Cache is invalidated on user updates/deletions
- Cache keys follow pattern: `user:{userId}`

## Event Streaming

User events are published to Kafka topic `user-events`:

- USER_REGISTERED
- USER_UPDATED
- USER_DELETED
- USER_ENABLED
- USER_DISABLED
- USER_LOCKED
- USER_UNLOCKED

Event payload includes:
```json
{
  "eventType": "USER_REGISTERED",
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "timestamp": "ISO-8601"
}
```

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

## Production Deployment

### Environment Variables

Ensure these are properly configured in production:

```properties
# Use strong JWT secret (at least 256 bits)
JWT_SECRET=<generate-strong-secret>

# Production database credentials
DB_HOST=<production-db-host>
DB_USERNAME=<db-user>
DB_PASSWORD=<strong-password>

# Redis configuration
REDIS_HOST=<redis-host>
REDIS_PASSWORD=<redis-password>

# Kafka configuration
KAFKA_BOOTSTRAP_SERVERS=<kafka-servers>

# OAuth2 configuration
OAUTH2_CLIENT_ID=<your-client-id>
OAUTH2_CLIENT_SECRET=<your-client-secret>
OAUTH2_ISSUER_URI=<issuer-uri>
```

### Build Production Image

```bash
docker build -t user-management-service:latest .
```

### Health Checks

The application provides health check endpoints:

- **Application**: `/api/v1/health`
- **Actuator**: `/actuator/health`

## Monitoring

### Metrics

Prometheus metrics are exposed at:

```
http://localhost:8080/actuator/prometheus
```

Key metrics include:
- HTTP request rates and latency
- Database connection pool stats
- Cache hit/miss rates
- Kafka producer/consumer metrics
- JVM memory and GC metrics

### Logging

Structured JSON logging is configured with appropriate log levels:

- **Development**: DEBUG
- **Production**: INFO/WARN

Log files are stored in `logs/` directory.

## Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# View PostgreSQL logs
docker logs user-management-postgres
```

### Redis Connection Issues

```bash
# Test Redis connection
docker exec -it user-management-redis redis-cli ping
```

### Kafka Issues

```bash
# List Kafka topics
docker exec -it user-management-kafka kafka-topics --list --bootstrap-server localhost:9092

# View Kafka logs
docker logs user-management-kafka
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For issues and questions, please open an issue on GitHub or contact support@example.com.
