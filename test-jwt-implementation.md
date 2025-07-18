# JWT Implementation Testing Guide

## Prerequisites
1. Start Redis server: `redis-server`
2. Start UserAuthService: `mvn spring-boot:run` (port 8080)
3. Start ProductService: `mvn spring-boot:run` (port 8081)

## Test Scenarios

### 1. Test User Registration (UserAuthService)
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "phoneNumber": "+1234567890"
  }'
```

**Expected Response:**
```json
{
  "message": "User registered successfully",
  "success": true,
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "roles": [{"id": 1, "roleName": "USER", "roleDescription": "Default user role"}],
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

### 2. Test User Login with JWT Generation
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "password": "password123",
    "rememberMe": false
  }'
```

**Expected Response:**
```json
{
  "message": "Login successful",
  "success": true,
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "roles": [{"id": 1, "roleName": "USER", "roleDescription": "Default user role"}]
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": "2024-02-01T10:00:00"
}
```

### 3. Test Public ProductService Endpoints (No JWT Required)
```bash
# Get all products (should work without token)
curl -X GET http://localhost:8081/products/

# Get single product (should work without token)
curl -X GET http://localhost:8081/products/1
```

### 4. Test Protected ProductService Endpoints (JWT Required)
```bash
# Replace YOUR_JWT_TOKEN with actual token from login response
export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Test authentication check
curl -X GET http://localhost:8081/products/me \
  -H "Authorization: Bearer $JWT_TOKEN"

# Try to create product without ADMIN role (should fail)
curl -X POST http://localhost:8081/products/ \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Product",
    "price": 100,
    "description": "Test Description",
    "category": {"title": "Electronics"}
  }'
```

**Expected Response (403 Forbidden):**
```json
{
  "error": "Access Denied"
}
```

### 5. Test Token Validation
```bash
curl -X POST http://localhost:8080/auth/validate-token \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response:**
```json
{
  "valid": true,
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "roles": [{"id": 1, "roleName": "USER", "roleDescription": "Default user role"}]
  },
  "message": "Valid token"
}
```

### 6. Test Logout (Token Blacklisting)
```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response:**
```
Logout successful
```

### 7. Test Token After Logout (Should Fail)
```bash
curl -X POST http://localhost:8080/auth/validate-token \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response (401 Unauthorized):**
```json
{
  "valid": false,
  "user": null,
  "message": "Token revoked"
}
```

### 8. Create Admin User and Test Admin Endpoints
First, manually create an admin user in the database or modify the user role:

```sql
-- Connect to MySQL
USE userauthproj;

-- Update user role to ADMIN
UPDATE user_roles SET role_id = (SELECT id FROM roles WHERE role_name = 'ADMIN') 
WHERE user_id = 1;
```

Then login as admin and test protected endpoints:

```bash
# Login as admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "password": "password123"
  }'

# Use admin token for product creation
export ADMIN_JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8081/products/ \
  -H "Authorization: Bearer $ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Admin Created Product",
    "price": 200,
    "description": "Product created by admin",
    "category": {"title": "Electronics"}
  }'
```

## Error Scenarios to Test

### 1. Invalid Token
```bash
curl -X GET http://localhost:8081/products/me \
  -H "Authorization: Bearer invalid_token"
```

### 2. Expired Token
Wait for token to expire (or set short expiration for testing) and try to use it.

### 3. No Token for Protected Endpoint
```bash
curl -X POST http://localhost:8081/products/ \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Product",
    "price": 100
  }'
```

### 4. Wrong Role for Endpoint
Use USER token to access ADMIN endpoint.

## Verification Points

✅ **JWT Generation**: Login returns valid JWT token with user info  
✅ **Token Validation**: ProductService validates tokens via UserAuthService  
✅ **Public Endpoints**: GET requests work without authentication  
✅ **Protected Endpoints**: Admin operations require ADMIN role  
✅ **Token Blacklisting**: Logout invalidates token  
✅ **User Banning**: Banned users can't access services  
✅ **Role-based Access**: Different endpoints require different roles  
✅ **Error Handling**: Proper error responses for invalid tokens  

## Performance Testing
For production, test:
- Token validation latency
- Redis performance under load
- UserAuthService response times
- Concurrent user scenarios 