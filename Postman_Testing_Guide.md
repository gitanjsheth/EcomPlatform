# Postman Testing Guide for JWT Authentication 🚀

## Prerequisites
1. **Start Redis**: `redis-server`
2. **Start UserAuthService**: `cd UserAuthService && ./mvnw spring-boot:run` (Port 8080)
3. **Start ProductService**: `cd ProductService && ./mvnw spring-boot:run` (Port 8081)

## 📋 Postman Collection Setup

### 1. Create New Collection
1. Open Postman
2. Click "New" → "Collection"
3. Name it "Ecommerce JWT Authentication"
4. Add description: "JWT Authentication testing for UserAuthService and ProductService"

### 2. Environment Variables Setup
Create a new environment called "JWT Auth Environment":

| Variable Name | Initial Value | Current Value |
|--------------|---------------|---------------|
| `userAuthBaseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `productBaseUrl` | `http://localhost:8081` | `http://localhost:8081` |
| `jwtToken` | (empty) | (will be set automatically) |
| `userId` | (empty) | (will be set automatically) |

## 🔐 Authentication Tests

### Test 1: User Registration
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/signup`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "phoneNumber": "+1234567890"
}
```

**Expected Response**: `201 Created`
```json
{
    "message": "User registered successfully",
    "success": true,
    "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "roles": [
            {
                "id": 1,
                "roleName": "USER",
                "roleDescription": "Default user role"
            }
        ],
        "createdAt": "2024-01-01T10:00:00"
    }
}
```

**Test Script** (Add to Tests tab):
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response contains user data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.be.true;
    pm.expect(jsonData.user.username).to.eql("testuser");
    pm.expect(jsonData.user.email).to.eql("test@example.com");
    
    // Store user ID for later use
    pm.environment.set("userId", jsonData.user.id);
});
```

### Test 2: User Login (Get JWT Token)
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/login`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "identifier": "test@example.com",
    "password": "password123",
    "rememberMe": false
}
```

**Expected Response**: `200 OK`
```json
{
    "message": "Login successful",
    "success": true,
    "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "roles": [
            {
                "id": 1,
                "roleName": "USER"
            }
        ]
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2024-02-01T10:00:00"
}
```

**Test Script** (Add to Tests tab):
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response contains JWT token", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.be.true;
    pm.expect(jsonData.token).to.exist;
    pm.expect(jsonData.expiresAt).to.exist;
    
    // Store JWT token for use in other requests
    pm.environment.set("jwtToken", jsonData.token);
});

pm.test("User data is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.user.username).to.eql("testuser");
    pm.expect(jsonData.user.email).to.eql("test@example.com");
});
```

### Test 3: Admin User Registration
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/signup`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "phoneNumber": "+1987654321"
}
```

**Note**: You'll need to manually update this user's role to ADMIN in the database, or create an admin endpoint for this.

### Test 4: Admin Login
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/login`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "identifier": "admin@example.com",
    "password": "admin123",
    "rememberMe": false
}
```

**Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Admin login successful", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.be.true;
    pm.expect(jsonData.token).to.exist;
    
    // Store admin token separately if needed
    pm.environment.set("adminToken", jsonData.token);
});
```

### Test 5: Token Validation
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/validate-token`  
**Headers**: 
```
Authorization: Bearer {{jwtToken}}
```

**Expected Response**: `200 OK`
```json
{
    "valid": true,
    "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "roles": [
            {
                "id": 1,
                "roleName": "USER"
            }
        ]
    },
    "message": "Valid token"
}
```

**Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Token is valid", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.valid).to.be.true;
    pm.expect(jsonData.user).to.exist;
});
```

### Test 6: Invalid Login
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/login`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "identifier": "test@example.com",
    "password": "wrongpassword",
    "rememberMe": false
}
```

**Expected Response**: `401 Unauthorized`

**Test Script**:
```javascript
pm.test("Status code is 401", function () {
    pm.response.to.have.status(401);
});

pm.test("Login failed", function () {
    // Response should indicate failure
    pm.expect(pm.response.text()).to.include("Invalid");
});
```

### Test 7: Logout (Token Blacklisting)
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/logout`  
**Headers**: 
```
Authorization: Bearer {{jwtToken}}
```

**Expected Response**: `200 OK`
```
Logout successful
```

**Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Logout successful", function () {
    pm.expect(pm.response.text()).to.include("successful");
});
```

## 🛍️ ProductService Tests

### Test 8: Get All Products (Public)
**Method**: `GET`  
**URL**: `{{productBaseUrl}}/products`  
**Headers**: None required

**Expected Response**: `200 OK`
```json
[
    {
        "id": 1,
        "title": "Sample Product",
        "price": 99,
        "description": "Sample description",
        "imageURL": "http://example.com/image.jpg",
        "category": {
            "id": 1,
            "title": "Electronics"
        }
    }
]
```

**Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    pm.expect(pm.response.json()).to.be.an('array');
});
```

### Test 9: Get Single Product (Public)
**Method**: `GET`  
**URL**: `{{productBaseUrl}}/products/1`  
**Headers**: None required

**Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Product has required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.title).to.exist;
    pm.expect(jsonData.price).to.exist;
});
```

### Test 10: Create Product Without Auth (Should Fail)
**Method**: `POST`  
**URL**: `{{productBaseUrl}}/products`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "title": "New Product",
    "description": "New product description",
    "price": 199,
    "imageURL": "http://example.com/newimage.jpg",
    "category": {
        "id": 1
    }
}
```

**Expected Response**: `403 Forbidden`

**Test Script**:
```javascript
pm.test("Status code is 403", function () {
    pm.response.to.have.status(403);
});
```

### Test 11: Create Product With Valid Admin Token
**Method**: `POST`  
**URL**: `{{productBaseUrl}}/products`  
**Headers**: 
```
Content-Type: application/json
Authorization: Bearer {{adminToken}}
```
**Body** (raw JSON):
```json
{
    "title": "Admin Created Product",
    "description": "Product created by admin",
    "price": 299,
    "imageURL": "http://example.com/admin-product.jpg",
    "category": {
        "id": 1
    }
}
```

**Expected Response**: `200 OK`

**Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Product created successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.title).to.eql("Admin Created Product");
});
```

### Test 12: Create Product With User Token (Should Fail)
**Method**: `POST`  
**URL**: `{{productBaseUrl}}/products`  
**Headers**: 
```
Content-Type: application/json
Authorization: Bearer {{jwtToken}}
```
**Body** (raw JSON):
```json
{
    "title": "User Attempted Product",
    "description": "This should fail",
    "price": 99,
    "imageURL": "http://example.com/fail.jpg",
    "category": {
        "id": 1
    }
}
```

**Expected Response**: `403 Forbidden`

**Test Script**:
```javascript
pm.test("Status code is 403", function () {
    pm.response.to.have.status(403);
});
```

## 🧪 Advanced Testing Scenarios

### Test 13: Account Locking (5 Failed Attempts)
Create 5 requests with the wrong password to test account locking:

**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/login`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "identifier": "test@example.com",
    "password": "wrongpassword",
    "rememberMe": false
}
```

Run this 5 times, then try with correct password - should still fail due to account lock.

### Test 14: Remember Me Token
**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/login`  
**Headers**: 
```
Content-Type: application/json
```
**Body** (raw JSON):
```json
{
    "identifier": "test@example.com",
    "password": "password123",
    "rememberMe": true
}
```

Check that the `expiresAt` date is 90 days in the future.

### Test 15: Token After Logout (Should Fail)
After running the logout test, try to use the token again:

**Method**: `POST`  
**URL**: `{{userAuthBaseUrl}}/auth/validate-token`  
**Headers**: 
```
Authorization: Bearer {{jwtToken}}
```

**Expected Response**: `401 Unauthorized` with message "Token revoked"

## 📊 Test Execution Order

1. **User Registration** → **User Login** → **Token Validation**
2. **Admin Registration** → **Admin Login**
3. **Public ProductService Tests**
4. **Protected ProductService Tests** (with and without proper tokens)
5. **Advanced Scenarios** (account locking, logout, etc.)

## 🔧 Pre-request Scripts

Add this to your collection's Pre-request Script tab for automatic token management:

```javascript
// Auto-refresh token if needed
if (pm.environment.get("jwtToken") && pm.request.headers.get("Authorization")) {
    console.log("Using stored JWT token for request");
}
```

## 📝 Notes

- Make sure Redis is running before testing logout/blacklisting features
- For admin tests, you may need to manually update user roles in the database
- Some tests depend on previous tests (like login before using tokens)
- Environment variables help manage tokens across different requests

This setup provides comprehensive testing of your JWT authentication system! 🎉 