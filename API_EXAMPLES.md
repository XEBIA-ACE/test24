# API Examples

This document provides practical examples of using the User Management Service API.

## Authentication Examples

### 1. Register a New User

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "Alice@123456",
    "firstName": "Alice",
    "lastName": "Smith",
    "phoneNumber": "+1234567890"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "alice",
    "email": "alice@example.com",
    "firstName": "Alice",
    "lastName": "Smith",
    "phoneNumber": "+1234567890",
    "isActive": true,
    "isEmailVerified": false,
    "roles": ["ROLE_USER"],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 2. Login

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "alice",
    "password": "Alice@123456"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJ1c2VybmFtZSI6ImFsaWNlIiwiZW1haWwiOiJhbGljZUBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3MDUzMTcwMDAsImV4cCI6MTcwNTMyMDYwMH0.abc123",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE3MDUzMTcwMDAsImV4cCI6MTcwNTQwMzQwMH0.def456",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "alice",
      "email": "alice@example.com",
      "firstName": "Alice",
      "lastName": "Smith",
      "isActive": true,
      "isEmailVerified": false,
      "roles": ["ROLE_USER"]
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 3. Refresh Access Token

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": { ... }
  }
}
```

### 4. Logout

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null,
  "timestamp": "2024-01-15T11:00:00"
}
```

## User Management Examples

### 5. Get User by ID (Authenticated)

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "alice",
    "email": "alice@example.com",
    "firstName": "Alice",
    "lastName": "Smith",
    "phoneNumber": "+1234567890",
    "isActive": true,
    "isEmailVerified": true,
    "lastLoginAt": "2024-01-15T10:30:00",
    "roles": ["ROLE_USER"],
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T11:00:00"
}
```

### 6. Get All Users (Admin Only, Paginated)

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/users?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "username": "alice",
        "email": "alice@example.com",
        "firstName": "Alice",
        "lastName": "Smith",
        "roles": ["ROLE_USER"]
      },
      {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "username": "bob",
        "email": "bob@example.com",
        "firstName": "Bob",
        "lastName": "Johnson",
        "roles": ["ROLE_USER"]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      }
    },
    "totalPages": 1,
    "totalElements": 2,
    "last": true,
    "first": true,
    "numberOfElements": 2
  }
}
```

### 7. Update User

**Request:**
```bash
curl -X PUT http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "phoneNumber": "+1234567891"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "alice",
    "email": "alice@example.com",
    "firstName": "Alice",
    "lastName": "Johnson",
    "phoneNumber": "+1234567891",
    "isActive": true,
    "isEmailVerified": true,
    "roles": ["ROLE_USER"],
    "updatedAt": "2024-01-15T11:30:00"
  }
}
```

### 8. Deactivate User (Admin Only)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000/deactivate \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "success": true,
  "message": "User deactivated successfully",
  "data": null,
  "timestamp": "2024-01-15T12:00:00"
}
```

### 9. Delete User (Admin Only)

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T12:00:00"
}
```

## Error Response Examples

### Validation Error

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ab",
    "email": "invalid-email",
    "password": "weak"
  }'
```

**Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Email must be valid",
    "password": "Password must be between 8 and 100 characters",
    "firstName": "First name is required",
    "lastName": "Last name is required"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Authentication Error

**Response:**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": "Invalid username/email or password",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Authorization Error

**Response:**
```json
{
  "success": false,
  "message": "Access denied",
  "error": "You don't have permission to access this resource",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Resource Not Found

**Response:**
```json
{
  "success": false,
  "message": "Resource not found",
  "error": "User not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Health Check Examples

### Application Health

**Request:**
```bash
curl -X GET http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

## Using with Postman

1. Import the API collection by accessing: http://localhost:8080/v3/api-docs
2. Create an environment with variables:
   - `base_url`: http://localhost:8080
   - `access_token`: (will be set after login)
3. Set up pre-request scripts to automatically add the Bearer token

## Using with HTTPie

Install HTTPie: `pip install httpie`

**Register:**
```bash
http POST :8080/api/v1/auth/register \
  username=alice \
  email=alice@example.com \
  password=Alice@123456 \
  firstName=Alice \
  lastName=Smith
```

**Login:**
```bash
http POST :8080/api/v1/auth/login \
  usernameOrEmail=alice \
  password=Alice@123456
```

**Get User (with token):**
```bash
http GET :8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  "Authorization: Bearer YOUR_ACCESS_TOKEN"
```
