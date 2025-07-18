# Cross-Service Authentication Test Guide

## Overview
This guide demonstrates how JWT authentication works across services in the ecommerce platform. A user authenticates with **UserAuthService** and uses the JWT token to access protected endpoints in **ProductService**.

## Architecture Flow
```
User → UserAuthService (Login) → JWT Token → ProductService (Validate Token) → Access Granted
```

1. **UserAuthService** (Port 8080): Issues JWT tokens after authentication
2. **ProductService** (Port 8081): Validates JWT tokens by calling UserAuthService
3. **Cross-Service Validation**: ProductService calls UserAuthService's `/auth/validate-token` endpoint

## Prerequisites

### 1. Services Running
- **UserAuthService**: `http://localhost:8080`
- **ProductService**: `http://localhost:8081`
- **Redis**: Running locally (for token blacklisting)

### 2. Postman Environment Setup
Create a Postman environment with these variables:
```
userAuthBaseUrl: http://localhost:8080
productBaseUrl: http://localhost:8081
jwtToken: (will be auto-populated)
userId: (will be auto-populated)
```

## New User-Specific Endpoints in ProductService

### Wishlist Management (User-Only Access)
- `GET /products/wishlist` - Get user's wishlist
- `POST /products/wishlist/{productId}` - Add product to wishlist  
- `DELETE /products/wishlist/{productId}` - Remove product from wishlist

### User Profile
- `GET /products/user/profile` - Get user profile via ProductService

### Authentication Check
- `GET /products/me` - Check current authentication status

## Test Sequence

### Step 1: Register User in UserAuthService
```http
POST http://localhost:8080/auth/signup
Content-Type: application/json

{
    "username": "testuser",
    "email": "test@example.com", 
    "password": "password123",
    "phoneNumber": "+1234567890"
}
```

**Expected**: 201 Created with user details

### Step 2: Login and Get JWT Token
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "usernameOrEmail": "testuser",
    "password": "password123"
}
```

**Expected**: 200 OK with JWT token
**Action**: Store the `token` value for subsequent requests

### Step 3: Test Public Access (No Auth Required)
```http
GET http://localhost:8081/products
```

**Expected**: 200 OK - Public endpoints accessible without authentication

### Step 4: Test Protected Access Without Token (Should Fail)
```http
GET http://localhost:8081/products/wishlist
```

**Expected**: 401 Unauthorized or 403 Forbidden - Protected endpoint blocked

### Step 5: Access Wishlist with JWT Token
```http
GET http://localhost:8081/products/wishlist
Authorization: Bearer {JWT_TOKEN}
```

**Expected**: 200 OK with wishlist data
**Verification**: 
- Response contains `userId`, `username`, `wishlistItems`
- Confirms cross-service authentication working

### Step 6: Add Product to Wishlist
```http
POST http://localhost:8081/products/wishlist/1
Authorization: Bearer {JWT_TOKEN}
```

**Expected**: 200 OK with confirmation message
**Verification**: Message includes username and product ID

### Step 7: Get User Profile via ProductService
```http
GET http://localhost:8081/products/user/profile  
Authorization: Bearer {JWT_TOKEN}
```

**Expected**: 200 OK with user profile
**Verification**:
- Contains user details from UserAuthService
- Shows `accessLevel: "Regular User"` (not admin)
- Confirms JWT properly decoded

### Step 8: Test Admin Endpoint (Should Fail for Regular User)
```http
POST http://localhost:8081/products
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "title": "Test Product",
    "description": "Test Description", 
    "price": 99,
    "imageUrl": "http://example.com/image.jpg",
    "category": {
        "name": "Test Category"
    }
}
```

**Expected**: 403 Forbidden - Regular users cannot create products

### Step 9: Remove Product from Wishlist
```http
DELETE http://localhost:8081/products/wishlist/1
Authorization: Bearer {JWT_TOKEN}
```

**Expected**: 200 OK with removal confirmation

## What This Demonstrates

### ✅ Cross-Service JWT Validation
- ProductService successfully validates JWT tokens by calling UserAuthService
- User identity and roles properly transmitted between services
- Stateless authentication working across microservices

### ✅ Role-Based Access Control
- Regular users can access user-specific endpoints
- Regular users blocked from admin endpoints  
- Proper role enforcement across services

### ✅ Security Layers
- Public endpoints accessible without authentication
- Protected endpoints require valid JWT
- Admin endpoints require admin role
- Invalid/missing tokens properly rejected

### ✅ User Session Management
- User identity maintained across multiple requests
- JWT token carries all necessary user information
- No session storage required in ProductService

## Automated Postman Testing

Import the `Ecommerce_JWT_Auth.postman_collection.json` and run the **ProductService Cross-Authentication** folder to execute all tests automatically.

### Key Tests:
1. **Public Access Test** - Confirms public endpoints work
2. **Unauthorized Access Test** - Confirms protection works  
3. **Cross-Service Auth Test** - Main JWT validation test
4. **User Operations Test** - Wishlist management
5. **Profile Access Test** - User data retrieval
6. **Admin Block Test** - Role-based protection
7. **Complete Flow Test** - End-to-end validation

## Troubleshooting

### Common Issues:

**401 Unauthorized on ProductService**
- Check JWT token is valid and not expired
- Verify UserAuthService is running on port 8080
- Check Redis is running (token blacklist service)

**403 Forbidden**  
- User trying to access admin endpoint
- Check user role in database (should be USER, not ADMIN)

**Connection Refused**
- Verify both services are running
- Check port configuration (8080 for auth, 8081 for products)
- Verify application.properties settings

**Token Validation Fails**
- Check UserAuthService `/auth/validate-token` endpoint is working
- Verify JWT secret key matches between services
- Check network connectivity between services

## Success Criteria

✅ **Authentication Flow**: User can login via UserAuthService and use JWT in ProductService  
✅ **Authorization**: Role-based access control working across services  
✅ **Security**: Unauthorized access properly blocked  
✅ **User Experience**: Seamless access to user-specific features  
✅ **Microservice Architecture**: Services properly isolated yet integrated

## Next Steps

1. **Load Testing**: Test with multiple concurrent users
2. **Token Refresh**: Implement automatic token renewal
3. **Service Discovery**: Add service registry for production
4. **Monitoring**: Add logging and metrics for cross-service calls
5. **Circuit Breaker**: Add resilience patterns for service failures 