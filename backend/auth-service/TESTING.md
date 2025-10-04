# Auth Service Manual Testing Guide

This guide shows you how to test the Auth Service manually using curl commands or tools like Postman.

## Prerequisites

1. **Start the services:**
   ```bash
   cd /Users/kiron/Software/ems
   docker-compose --profile backend up -d postgres redis
   ```

2. **Build and run the auth service:**
   ```bash
   cd backend/auth-service
   mvn clean package
   java -jar target/auth-service-1.0.0.jar
   ```

   The service will start on port 8081.

## API Endpoints Testing

### 1. User Registration

**Endpoint:** `POST /api/auth/register`

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "StrongPassword123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "OPERATOR"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "OPERATOR",
  "createdAt": "2025-10-04T10:30:00"
}
```

### 2. User Authentication/Login

**Endpoint:** `POST /api/auth/login`

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "StrongPassword123"
  }'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "OPERATOR",
    "createdAt": "2025-10-04T10:30:00"
  }
}
```

### 3. Get User Profile

**Endpoint:** `GET /api/auth/profile?email={email}`

```bash
curl -X GET "http://localhost:8081/api/auth/profile?email=john.doe@example.com"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "OPERATOR",
  "createdAt": "2025-10-04T10:30:00"
}
```

### 4. Forgot Password (Generate Reset Token)

**Endpoint:** `POST /api/auth/forgot-password`

```bash
curl -X POST http://localhost:8081/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'
```

**Expected Response (200 OK):**
```json
{
  "message": "Password reset instructions sent to your email"
}
```

### 5. Reset Password (Using Reset Token)

**Endpoint:** `POST /api/auth/reset-password`

First, get the reset token from the database:
```sql
SELECT password_reset_token FROM users WHERE email = 'john.doe@example.com';
```

Then use the token:
```bash
curl -X POST http://localhost:8081/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "resetToken": "your-reset-token-here",
    "newPassword": "NewStrongPassword456"
  }'
```

**Expected Response (200 OK):**
```json
{
  "message": "Password reset successfully"
}
```

### 6. Change Password

**Endpoint:** `POST /api/auth/change-password`

```bash
curl -X POST http://localhost:8081/api/auth/change-password \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "StrongPassword123",
    "newPassword": "NewStrongPassword789"
  }'
```

**Expected Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

## Error Testing Scenarios

### 1. Registration with Existing Email
```bash
# Register the same user twice
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "AnotherPassword123",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "VIEWER"
  }'
```
**Expected Response (400 Bad Request):**
```json
{
  "message": "User with email john.doe@example.com already exists"
}
```

### 2. Registration with Weak Password
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "weak@example.com",
    "password": "weak",
    "firstName": "Weak",
    "lastName": "Password",
    "role": "VIEWER"
  }'
```
**Expected Response (400 Bad Request):**
```json
{
  "message": "Password must be at least 8 characters long"
}
```

### 3. Authentication with Wrong Password
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "WrongPassword123"
  }'
```
**Expected Response (401 Unauthorized):**
```json
{
  "message": "Invalid email or password"
}
```

### 4. Get Profile for Non-existent User
```bash
curl -X GET "http://localhost:8081/api/auth/profile?email=nonexistent@example.com"
```
**Expected Response (404 Not Found):**
```json
{
  "message": "User not found"
}
```

## Database Verification

You can verify the operations by connecting to the PostgreSQL database:

```bash
# Connect to PostgreSQL
docker exec -it ems-postgres psql -U ems_user -d ems_auth

# Check users table
SELECT id, email, first_name, last_name, role, account_enabled, created_at, last_login 
FROM users;

# Check password hash (should be bcrypt encoded)
SELECT email, password_hash FROM users WHERE email = 'john.doe@example.com';
```

## JWT Token Verification

You can decode the JWT tokens using online tools like [jwt.io](https://jwt.io) or using command line:

```bash
# Example JWT token structure (header.payload.signature)
# The payload should contain:
{
  "sub": "john.doe@example.com",
  "role": "OPERATOR",
  "iat": 1728042600,
  "exp": 1728046200
}
```

## Testing with Different User Roles

Create users with different roles to test role-based functionality:

```bash
# Admin user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPassword123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'

# Viewer user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "viewer@example.com",
    "password": "ViewerPassword123",
    "firstName": "Viewer",
    "lastName": "User",
    "role": "VIEWER"
  }'
```

## Health Check

Verify the service is running:
```bash
curl http://localhost:8081/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

## Notes

1. **Password Requirements:**
   - At least 8 characters long
   - Must contain at least one uppercase letter
   - Must contain at least one lowercase letter  
   - Must contain at least one digit

2. **User Roles:**
   - `ADMIN`: Full system access
   - `OPERATOR`: Device management access
   - `VIEWER`: Read-only access

3. **JWT Token Expiration:**
   - Access tokens expire in 1 hour (3600000ms)
   - Refresh tokens expire in 7 days

4. **Database Tables:**
   - All user data is stored in the `users` table
   - Password reset tokens are stored in the same table with expiration