# Architecture Documentation

## Overview

The User Management Service is built following Clean Architecture principles with a clear separation of concerns across layers. The application is designed to be maintainable, testable, and scalable.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        API Layer                            │
│  ┌────────────┐  ┌────────────┐  ┌──────────────┐          │
│  │Controllers │  │   DTOs     │  │   Mappers    │          │
│  └────────────┘  └────────────┘  └──────────────┘          │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    Domain Layer                             │
│  ┌────────────┐  ┌────────────┐  ┌──────────────┐          │
│  │  Services  │  │  Entities  │  │ Repositories │          │
│  └────────────┘  └────────────┘  └──────────────┘          │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                Infrastructure Layer                         │
│  ┌──────────┐ ┌────────┐ ┌────────┐ ┌─────────┐           │
│  │PostgreSQL│ │ Redis  │ │ Kafka  │ │Security │           │
│  └──────────┘ └────────┘ └────────┘ └─────────┘           │
└─────────────────────────────────────────────────────────────┘
```

## Layer Details

### 1. API Layer

**Responsibilities:**
- Handle HTTP requests and responses
- Input validation
- DTO transformation
- Exception handling

**Components:**

- **Controllers** (`api.controller`):
  - `AuthController`: Authentication endpoints
  - `UserController`: User management endpoints
  - `HealthController`: Health check endpoints

- **DTOs** (`api.dto`):
  - Request DTOs for input validation
  - Response DTOs for consistent output format

- **Mappers** (`api.mapper`):
  - MapStruct-based entity-to-DTO conversion
  - Ensures separation between domain and API layers

### 2. Domain Layer

**Responsibilities:**
- Core business logic
- Domain entities
- Business rules and validations
- Data access abstraction

**Components:**

- **Services** (`domain.service`):
  - `UserService`: User business logic
  - `AuthService`: Authentication logic
  - `EventPublisherService`: Event publishing

- **Entities** (`domain.entity`):
  - `User`: User account data
  - `Role`: User roles
  - `Permission`: Fine-grained permissions
  - `RefreshToken`: Token management

- **Repositories** (`domain.repository`):
  - Spring Data JPA interfaces
  - Custom query methods

### 3. Infrastructure Layer

**Responsibilities:**
- External service integration
- Security configuration
- Database management
- Messaging

**Components:**

- **Security** (`security`):
  - JWT generation and validation
  - Spring Security configuration
  - OAuth 2.0 with PKCE support

- **Configuration** (`config`):
  - Database configuration
  - Redis caching setup
  - Kafka producer configuration
  - OpenAPI documentation

## Data Flow

### User Registration Flow

```
1. Client → POST /api/v1/auth/register
           ↓
2. AuthController validates request
           ↓
3. UserService.registerUser()
   - Check username/email uniqueness
   - Hash password
   - Assign default role
   - Generate verification token
           ↓
4. UserRepository saves user
           ↓
5. EventPublisherService → Kafka (user.created)
           ↓
6. Response with UserDTO
```

### Authentication Flow

```
1. Client → POST /api/v1/auth/login
           ↓
2. AuthController validates credentials
           ↓
3. AuthService.login()
   - Verify credentials
   - Check account status
   - Generate JWT tokens
           ↓
4. RefreshTokenRepository saves refresh token
           ↓
5. Response with AuthResponse (tokens + user)
```

### Authenticated Request Flow

```
1. Client → GET /api/v1/users/{id}
   Header: Authorization: Bearer {token}
           ↓
2. JwtAuthenticationFilter
   - Extract JWT
   - Validate token
   - Set SecurityContext
           ↓
3. UserController
   - Check authorization (@PreAuthorize)
           ↓
4. UserService.getUserById()
   - Check cache (Redis)
   - If not cached, fetch from DB
   - Cache result
           ↓
5. Response with UserDTO
```

## Database Schema

### Entity Relationships

```
┌──────────┐         ┌──────────────┐         ┌─────────────┐
│   User   │────────>│  user_roles  │<────────│    Role     │
└──────────┘    *    └──────────────┘    *    └─────────────┘
                                                      │
                                                      │ *
                                              ┌───────▼──────┐
                                              │role_permissions│
                                              └───────┬──────┘
                                                      │ *
                                              ┌───────▼──────┐
                                              │  Permission  │
                                              └──────────────┘

┌──────────┐         ┌──────────────┐
│   User   │────────>│RefreshToken  │
└──────────┘    1:N  └──────────────┘
```

### Key Tables

**users**
- Stores user account information
- Includes security fields (password_hash, failed_login_attempts, locked_until)
- Audit fields (created_at, updated_at)

**roles**
- Defines user roles (ADMIN, USER, MODERATOR)
- Many-to-many with users and permissions

**permissions**
- Granular permissions (resource + action)
- E.g., USER_READ, USER_CREATE, USER_DELETE

**refresh_tokens**
- Manages JWT refresh tokens
- Includes expiration and revocation

## Security Architecture

### Authentication

1. **JWT-based Authentication**:
   - Access tokens: Short-lived (1 hour default)
   - Refresh tokens: Long-lived (24 hours default)
   - Signed with HMAC-SHA256

2. **OAuth 2.0 with PKCE**:
   - Authorization code flow
   - PKCE for additional security
   - Configurable OAuth provider

### Authorization

1. **Role-Based Access Control (RBAC)**:
   - Roles assigned to users
   - Permissions assigned to roles
   - Method-level security with `@PreAuthorize`

2. **Resource-Level Security**:
   - Custom `UserSecurity` component
   - Checks ownership for sensitive operations

### Password Security

- BCrypt hashing (cost factor: 10)
- Strong password requirements enforced
- Account lockout after 5 failed attempts
- Lockout duration: 30 minutes

## Caching Strategy

### Redis Caching

**Cached Data:**
- User profiles (TTL: 1 hour)
- General data (TTL: 30 minutes)

**Cache Keys:**
- `users::{userId}`
- `users::{username}`

**Cache Invalidation:**
- Automatic on user updates
- Manual with `@CacheEvict`

## Event-Driven Architecture

### Kafka Topics

**user-events**
- Partitions: 3
- Replication: 1 (configure higher for production)

**Event Types:**
- `user.created`: New user registration
- `user.updated`: User information changed
- `user.deleted`: User account removed

**Event Schema:**
```json
{
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "timestamp": "iso8601"
}
```

## Observability

### Metrics

- Application metrics via Spring Boot Actuator
- Custom business metrics
- JVM metrics
- Database connection pool metrics

### Logging

**Log Levels:**
- ERROR: Application errors
- WARN: Warning conditions
- INFO: Important business events
- DEBUG: Detailed diagnostic information

**Structured Logging:**
- Correlation IDs for request tracing
- User context in logs
- Performance metrics

### Health Checks

**Endpoints:**
- `/actuator/health`: Overall health
- `/actuator/health/liveness`: Liveness probe
- `/actuator/health/readiness`: Readiness probe

**Checks:**
- Database connectivity
- Redis connectivity
- Disk space
- Custom health indicators

## Scalability Considerations

### Horizontal Scaling

- Stateless application design
- Session data in Redis
- Load balancer distribution

### Database Scaling

- Connection pooling (HikariCP)
- Read replicas for read-heavy workloads
- Database indexes on frequently queried columns

### Caching

- Redis for distributed caching
- Cache warming strategies
- Cache invalidation patterns

### Asynchronous Processing

- Kafka for event publishing
- Non-blocking operations where possible
- Async email sending (future enhancement)

## Performance Optimizations

1. **Database**:
   - Indexes on foreign keys and frequently queried columns
   - Batch operations for bulk updates
   - N+1 query prevention with JOIN FETCH

2. **Caching**:
   - Redis for frequently accessed data
   - Cache-aside pattern
   - Proper TTL configuration

3. **JVM**:
   - G1GC for consistent performance
   - Container-aware heap sizing
   - Proper memory limits

## Testing Strategy

### Unit Tests

- Service layer logic
- Utility functions
- Mocked dependencies

### Integration Tests

- Controller layer with MockMvc
- Repository layer with test containers
- End-to-end API tests

### Test Containers

- PostgreSQL container for DB tests
- Redis container for cache tests
- Kafka container for messaging tests

## Deployment Architecture

### Containerization

- Multi-stage Docker build
- Optimized image size
- Non-root user for security
- Health checks in Dockerfile

### Orchestration

- Kubernetes deployment
- Auto-scaling based on metrics
- Rolling updates for zero downtime
- Readiness and liveness probes

## Design Patterns Used

1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic encapsulation
3. **DTO Pattern**: API-domain separation
4. **Builder Pattern**: Object construction (Lombok)
5. **Strategy Pattern**: Multiple authentication methods
6. **Observer Pattern**: Event publishing
7. **Singleton Pattern**: Spring beans

## Future Enhancements

- [ ] Rate limiting per user/IP
- [ ] Two-factor authentication (2FA)
- [ ] Social login (Google, Facebook)
- [ ] Audit logging for compliance
- [ ] User activity tracking
- [ ] Advanced search capabilities
- [ ] Bulk operations API
- [ ] WebSocket support for real-time updates
- [ ] GraphQL API
- [ ] Multi-tenancy support
