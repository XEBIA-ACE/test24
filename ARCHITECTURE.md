# Architecture Overview

## System Architecture

This User Management Service follows **Clean Architecture** principles with clear separation of concerns across layers.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Controllers  │  │   Security   │  │  Exception   │          │
│  │   (REST)     │  │   Filters    │  │   Handlers   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Business Logic Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Services   │  │    Mappers   │  │    Events    │          │
│  │              │  │   (MapStruct)│  │   (Kafka)    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Data Access Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Repositories │  │    Cache     │  │   Entities   │          │
│  │   (JPA)      │  │   (Redis)    │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  PostgreSQL  │  │    Redis     │  │    Kafka     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. Presentation Layer

#### Controllers
- **AuthController**: Authentication endpoints (login, refresh, logout)
- **UserController**: User management CRUD operations
- **HealthController**: Health check endpoints

#### Security Components
- **JwtAuthenticationFilter**: Intercepts requests to validate JWT tokens
- **JwtAuthenticationEntryPoint**: Handles authentication errors
- **SecurityConfig**: Spring Security configuration

#### Exception Handling
- **GlobalExceptionHandler**: Centralized exception handling
- Custom exceptions: `ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`

### 2. Business Logic Layer

#### Services
- **UserService**: User management operations
- **AuthenticationService**: Authentication and token management
- **CacheService**: Caching abstraction
- **KafkaProducerService**: Event publishing

#### Mappers
- **UserMapper**: MapStruct-based DTO ↔ Entity mapping

#### DTOs
- Request DTOs: `UserRegistrationRequest`, `LoginRequest`, `UserUpdateRequest`
- Response DTOs: `UserResponse`, `AuthResponse`

### 3. Data Access Layer

#### Repositories (Spring Data JPA)
- **UserRepository**: User entity operations
- **RoleRepository**: Role entity operations
- **PermissionRepository**: Permission entity operations
- **RefreshTokenRepository**: Refresh token management

#### Entities
- **User**: Core user entity with audit fields
- **Role**: Role entity for RBAC
- **Permission**: Granular permission entity
- **RefreshToken**: Refresh token storage

#### Cache
- **RedisCacheService**: Redis-based caching implementation
- Caches user data with configurable TTL

### 4. Infrastructure

#### Database
- **PostgreSQL**: Primary data store
- **Flyway**: Database migration management

#### Cache
- **Redis**: Distributed caching for performance

#### Messaging
- **Kafka**: Event streaming for user lifecycle events

## Data Flow

### User Registration Flow

```
Client → UserController.registerUser()
         ↓
   UserService.registerUser()
         ↓ (validate username/email uniqueness)
   UserRepository.save()
         ↓ (persist to database)
   CacheService.set()
         ↓ (cache user data)
   KafkaProducer.sendUserEvent("USER_REGISTERED")
         ↓
   Return UserResponse
```

### Authentication Flow

```
Client → AuthController.login()
         ↓
   AuthenticationService.login()
         ↓ (authenticate via Spring Security)
   JwtTokenProvider.generateAccessToken()
         ↓
   JwtTokenProvider.generateRefreshToken()
         ↓
   RefreshTokenRepository.save()
         ↓ (store refresh token)
   UserService.updateLastLogin()
         ↓
   Return AuthResponse (with tokens)
```

### Protected Request Flow

```
Client → [JWT in Authorization Header]
         ↓
   JwtAuthenticationFilter.doFilterInternal()
         ↓ (validate token)
   JwtTokenProvider.validateToken()
         ↓ (extract user ID)
   CustomUserDetailsService.loadUserById()
         ↓ (set authentication in context)
   Controller method executes
         ↓
   Return response
```

## Security Architecture

### Authentication Mechanisms

1. **JWT (JSON Web Tokens)**
   - Access tokens: Short-lived (24 hours)
   - Refresh tokens: Long-lived (7 days)
   - Signed with HMAC-SHA256

2. **OAuth2 with PKCE** (configured, ready for integration)
   - Authorization code flow with PKCE
   - Resource server configuration
   - Token validation

### Authorization

1. **Role-Based Access Control (RBAC)**
   - Roles: USER, ADMIN, MODERATOR, SUPER_ADMIN
   - Assigned at registration (default: USER)

2. **Permission-Based Access**
   - Granular permissions per resource/action
   - Many-to-many mapping: Role → Permissions

3. **Method Security**
   - `@PreAuthorize` annotations on controller methods
   - SpEL expressions for fine-grained control

### Security Features

- Password hashing with BCrypt
- Account locking after 5 failed attempts
- Token revocation support
- IP tracking for refresh tokens
- CORS configuration
- CSRF protection (disabled for stateless API)

## Caching Strategy

### Cache-Aside Pattern

```
Request → Check Cache
           │
           ├─ Cache Hit → Return cached data
           │
           └─ Cache Miss → Fetch from DB
                           ↓
                        Update Cache
                           ↓
                        Return data
```

### Cached Data
- User responses (key: `user:{userId}`)
- TTL: 1 hour (configurable)

### Cache Invalidation
- On user update
- On user deletion
- On user status change

## Event-Driven Architecture

### User Events Published to Kafka

Event types:
- `USER_REGISTERED`
- `USER_UPDATED`
- `USER_DELETED`
- `USER_ENABLED`
- `USER_DISABLED`
- `USER_LOCKED`
- `USER_UNLOCKED`

### Event Payload

```json
{
  "eventType": "USER_REGISTERED",
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "timestamp": "ISO-8601 datetime"
}
```

### Use Cases
- Audit logging
- Analytics
- Email notifications
- Downstream service synchronization

## Database Schema

### Entity Relationships

```
User ←→ Role (Many-to-Many via user_roles)
  │
  └──→ RefreshToken (One-to-Many)

Role ←→ Permission (Many-to-Many via role_permissions)
```

### Key Design Decisions

1. **UUID Primary Keys**: Better for distributed systems
2. **Audit Fields**: `created_at`, `updated_at` on all entities
3. **Optimistic Locking**: `version` field on User entity
4. **Soft Delete**: Via `is_enabled` flag (no actual deletion)
5. **Indexes**: On frequently queried fields (username, email)

## Scalability Considerations

### Horizontal Scaling
- Stateless design (JWT-based auth)
- Externalized session storage (Redis)
- Load balancer ready

### Performance Optimizations
- Database connection pooling (HikariCP)
- Redis caching reduces DB load
- Indexed database queries
- Lazy loading of relationships

### Monitoring & Observability
- Actuator health checks
- Prometheus metrics
- Structured logging
- Request/response logging

## Technology Choices

### Why Spring Boot?
- Industry standard for Java microservices
- Rich ecosystem of libraries
- Production-ready features (Actuator)
- Excellent documentation

### Why PostgreSQL?
- ACID compliance
- Advanced features (JSON, full-text search)
- Strong community support
- Excellent performance

### Why Redis?
- High-performance in-memory storage
- Pub/sub capabilities (future extension)
- Distributed caching support
- Simple key-value operations

### Why Kafka?
- High-throughput message streaming
- Event sourcing support
- Decoupling services
- Reliable message delivery

## Best Practices Implemented

1. **Clean Architecture**: Clear separation of concerns
2. **SOLID Principles**: Dependency injection, single responsibility
3. **DTO Pattern**: Separation of API contracts from domain models
4. **Repository Pattern**: Abstraction over data access
5. **Service Layer**: Business logic encapsulation
6. **Exception Handling**: Consistent error responses
7. **Input Validation**: JSR-303 annotations
8. **Logging**: Structured logging with SLF4J
9. **Testing**: Unit and integration tests
10. **Documentation**: OpenAPI/Swagger specs

## Future Enhancements

1. **OAuth2 PKCE Integration**: Complete OAuth2 flow implementation
2. **Email Verification**: Email confirmation workflow
3. **Password Reset**: Forgot password functionality
4. **Two-Factor Authentication**: TOTP-based 2FA
5. **Rate Limiting**: API throttling
6. **GraphQL Support**: Alternative to REST
7. **Multi-tenancy**: Support for multiple organizations
8. **Advanced Analytics**: User behavior tracking
9. **Distributed Tracing**: OpenTelemetry integration
10. **Circuit Breaker**: Resilience4j integration
