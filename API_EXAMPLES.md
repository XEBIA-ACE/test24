# API Examples

Complete examples for testing the User Management Service API.

## Base URL

```
http://localhost:8080
```

## Authentication Flow

### 1. Register a New User

**Request:**
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

**Response (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "isEnabled": true,
  "isLocked": false,
  "emailVerified": false,
  "roles": ["ROLE_USER"],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 2. Login

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "SecurePass123!"
  }'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjNlNDU2Ny1lODliLTEyZDMtYTQ1Ni00MjY2MTQxNzQwMDAiLCJ1c2VybmFtZSI6ImpvaG5kb2UiLCJlbWFpbCI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwicm9sZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE3MDUzMjE4MDAsImV4cCI6MTcwNTQwODIwMH0.xyz",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjNlNDU2Ny1lODliLTEyZDMtYTQ1Ni00MjY2MTQxNzQwMDAiLCJpYXQiOjE3MDUzMjE4MDAsImV4cCI6MTcwNTkyNjYwMH0.abc",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "issuedAt": "2024-01-15T10:30:00",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "isEnabled": true,
    "isLocked": false,
    "emailVerified": false,
    "roles": ["ROLE_USER"],
    "lastLoginAt": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
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

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new.token",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "issuedAt": "2024-01-15T11:00:00",
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

### 4. Logout

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Response (200 OK):**
```
(Empty response body)
```

## User Management

### 5. Get User by ID

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "isEnabled": true,
  "isLocked": false,
  "emailVerified": false,
  "roles": ["ROLE_USER"],
  "lastLoginAt": "2024-01-15T10:30:00",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 6. Get All Users (Admin Only)

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/users?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "johndoe",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phoneNumber": "+1234567890",
      "isEnabled": true,
      "isLocked": false,
      "emailVerified": false,
      "roles": ["ROLE_USER"],
      "createdAt": "2024-01-15T10:30:00"
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "username": "janedoe",
      "email": "jane.doe@example.com",
      "firstName": "Jane",
      "lastName": "Doe",
      "isEnabled": true,
      "isLocked": false,
      "roles": ["ROLE_USER"],
      "createdAt": "2024-01-14T09:15:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 2,
  "last": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 2,
  "first": true,
  "empty": false
}
```

### 7. Update User

**Request:**
```bash
curl -X PUT http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "phoneNumber": "+1987654321"
  }'
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+1987654321",
  "isEnabled": true,
  "isLocked": false,
  "roles": ["ROLE_USER"],
  "updatedAt": "2024-01-15T11:00:00"
}
```

### 8. Delete User (Admin Only)

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (204 No Content):**
```
(Empty response body)
```

### 9. Enable User (Admin Only)

**Request:**
```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/enable \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "isEnabled": true,
  "isLocked": false,
  "roles": ["ROLE_USER"]
}
```

### 10. Disable User (Admin Only)

**Request:**
```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/disable \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "isEnabled": false,
  "isLocked": false,
  "roles": ["ROLE_USER"]
}
```

### 11. Lock User (Admin Only)

**Request:**
```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/lock \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "isEnabled": true,
  "isLocked": true,
  "roles": ["ROLE_USER"]
}
```

### 12. Unlock User (Admin Only)

**Request:**
```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/unlock \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "isEnabled": true,
  "isLocked": false,
  "roles": ["ROLE_USER"]
}
```

## Health Check

### 13. Application Health Check

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "user-management-service",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 14. Actuator Health Check

**Request:**
```bash
curl -X GET http://localhost:8080/actuator/health
```

**Response (200 OK):**
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
        "total": 500107862016,
        "free": 298999836672,
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

## Error Responses

### 400 Bad Request (Validation Error)

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users/register",
  "errors": {
    "password": "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character",
    "email": "Invalid email format"
  }
}
```

### 401 Unauthorized

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/v1/auth/login"
}
```

### 403 Forbidden

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/v1/users"
}
```

### 404 Not Found

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: '123e4567-e89b-12d3-a456-426614174000'",
  "path": "/api/v1/users/123e4567-e89b-12d3-a456-426614174000"
}
```

### 500 Internal Server Error

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later.",
  "path": "/api/v1/users/123e4567-e89b-12d3-a456-426614174000"
}
```

## Testing with Postman

Import this collection JSON to test all endpoints:

1. Create a new collection in Postman
2. Set up environment variables:
   - `baseUrl`: `http://localhost:8080`
   - `accessToken`: (will be set after login)
   - `refreshToken`: (will be set after login)

3. Add pre-request script for authenticated requests:
```javascript
pm.environment.get("accessToken") &&
pm.request.headers.add({
    key: 'Authorization',
    value: 'Bearer ' + pm.environment.get("accessToken")
});
```

4. Add test script for login endpoint:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("accessToken", jsonData.accessToken);
    pm.environment.set("refreshToken", jsonData.refreshToken);
}
```

## Testing with HTTPie

```bash
# Install HTTPie
pip install httpie

# Register user
http POST :8080/api/v1/users/register \
  username=johndoe \
  email=john.doe@example.com \
  password=SecurePass123! \
  firstName=John \
  lastName=Doe

# Login
http POST :8080/api/v1/auth/login \
  usernameOrEmail=johndoe \
  password=SecurePass123!

# Get user (with token)
http GET :8080/api/v1/users/{id} \
  Authorization:"Bearer {token}"
```
