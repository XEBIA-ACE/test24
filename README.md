# User Management Service

A production-ready, enterprise-grade User Management Service built with Spring Boot, featuring OAuth 2.0 with PKCE, JWT authentication, Redis caching, and Kafka event streaming.

## 🚀 Features

- **User Management**: Complete CRUD operations for user accounts
- **Authentication & Authorization**:
  - JWT-based authentication with access and refresh tokens
  - OAuth 2.0 with PKCE support for secure authorization
  - Role-based access control (RBAC) with permissions
- **Security**:
  - Password encryption with BCrypt
  - Account lockout after failed login attempts
  - Email verification workflow
  - Password reset functionality
- **Caching**: Redis-based caching for improved performance
- **Event Streaming**: Kafka integration for publishing user lifecycle events
- **API Documentation**: Interactive OpenAPI/Swagger documentation
- **Observability**:
  - Health checks and liveness probes
  - Prometheus metrics export
  - Structured logging
- **Database**: PostgreSQL with Flyway migrations
- **Docker Support**: Multi-stage Dockerfile and docker-compose setup

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 16+ (if running locally)
- Redis 7+ (if running locally)
- Kafka (if running locally)

## 🏗️ Architecture

The application follows Clean Architecture principles with clear separation of concerns:

```
src/
├── main/
│   ├── java/com/usermanagement/
│   │   ├── api/                    # API Layer
│   │   │   ├── controller/         # REST Controllers
│   │   │   ├── dto/                # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   └── mapper/             # Entity-DTO Mappers
│   │   ├── config/                 # Configuration Classes
│   │   ├── domain/                 # Domain Layer
│   │   │   ├── entity/             # JPA Entities
│   │   │   ├── repository/         # Data Access Layer
│   │   │   └── service/            # Business Logic
│   │   ├── event/                  # Event Models
│   │   ├── exception/              # Custom Exceptions
│   │   └── security/               # Security Components
│   └── resources/
│       ├── db/migration/           # Flyway Migrations
│       └── application*.yml        # Configuration Files
└── test/                           # Test Classes
```

## 🚀 Quick Start

### Option 1: Using Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone <repository-url>
cd user-management-service
```

2. Create environment file:
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. Start all services:
```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`

### Option 2: Local Development

1. Start required services (PostgreSQL, Redis, Kafka):
```bash
docker-compose up -d postgres redis zookeeper kafka
```

2. Configure environment variables:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=user_management
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export REDIS_HOST=localhost
export REDIS_PORT=6379
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export JWT_SECRET=your-256-bit-secret-key-change-this-in-production
```

3. Run database migrations:
```bash
mvn flyway:migrate
```

4. Build and run the application:
```bash
mvn clean package
java -jar target/user-management-service-1.0.0.jar
```

Or run directly with Maven:
```bash
mvn spring-boot:run
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Key Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Login and receive JWT tokens |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Logout and revoke refresh token |
| GET | `/api/v1/auth/verify-email?token={token}` | Verify email address |

#### User Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/v1/users` | Get all users (paginated) | ADMIN |
| GET | `/api/v1/users/{id}` | Get user by ID | ADMIN or Owner |
| GET | `/api/v1/users/username/{username}` | Get user by username | ADMIN or Owner |
| PUT | `/api/v1/users/{id}` | Update user | ADMIN or Owner |
| DELETE | `/api/v1/users/{id}` | Delete user | ADMIN |
| POST | `/api/v1/users/{id}/activate` | Activate user account | ADMIN |
| POST | `/api/v1/users/{id}/deactivate` | Deactivate user account | ADMIN |

#### Health & Monitoring

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Simple health check |
| GET | `/actuator/health` | Detailed health information |
| GET | `/actuator/metrics` | Application metrics |
| GET | `/actuator/prometheus` | Prometheus metrics |

## 🔐 Authentication Flow

### Registration & Login

1. **Register a new user**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecureP@ss123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

2. **Login**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "SecureP@ss123"
  }'
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
  }
}
```

3. **Access protected endpoints**:
```bash
curl -X GET http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Token Refresh

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `user_management` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | - |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token expiration (ms) | `3600000` (1 hour) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token expiration (ms) | `86400000` (24 hours) |

### Application Profiles

- **dev**: Development profile with debug logging
- **prod**: Production profile with optimized settings
- **test**: Test profile for running tests

Switch profiles using:
```bash
export SPRING_PROFILES_ACTIVE=prod
```

## 🗄️ Database Schema

### Main Tables

- **users**: User account information
- **roles**: Role definitions
- **permissions**: Permission definitions
- **user_roles**: User-role associations
- **role_permissions**: Role-permission associations
- **refresh_tokens**: Active refresh tokens

### Default Roles

- **ROLE_ADMIN**: Full system access
- **ROLE_MODERATOR**: Elevated privileges for user management
- **ROLE_USER**: Standard user access

## 📊 Monitoring & Observability

### Health Checks

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

Access Prometheus metrics:
```bash
curl http://localhost:8080/actuator/prometheus
```

### Grafana Dashboard

Access Grafana at http://localhost:3000 (default credentials: admin/admin)

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
```

### Test Coverage
```bash
mvn jacoco:report
```

## 🔧 Development

### Build the Project
```bash
mvn clean package
```

### Run Locally
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Build Docker Image
```bash
docker build -t user-management-service:latest .
```

### Database Migrations

Create a new migration:
```bash
# Create file: src/main/resources/db/migration/V{version}__{description}.sql
```

Run migrations:
```bash
mvn flyway:migrate
```

## 📦 Kafka Events

The service publishes the following events to the `user-events` topic:

- **user.created**: When a new user registers
- **user.updated**: When user information is updated
- **user.deleted**: When a user is deleted

Event Schema:
```json
{
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string"
}
```

## 🔒 Security Considerations

### Production Deployment

1. **Change JWT Secret**: Use a strong, randomly generated secret
2. **Enable HTTPS**: Configure SSL/TLS certificates
3. **Database Security**: Use strong passwords and restrict access
4. **Rate Limiting**: Implement API rate limiting
5. **CORS Configuration**: Restrict allowed origins
6. **Environment Variables**: Never commit secrets to version control

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%*?&)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 🆘 Support

For issues and questions:
- Create an issue in the repository
- Contact: support@usermanagement.com

## 🙏 Acknowledgments

- Spring Boot Team
- PostgreSQL Community
- Redis Community
- Apache Kafka Team
