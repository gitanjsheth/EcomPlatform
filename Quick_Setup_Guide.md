# Quick Setup Guide for Postman Testing 🚀

## 🔧 Setup Steps (Do these first!)

### 1. Install and Start Redis

#### Option A: Using Homebrew (Mac - Recommended)
```bash
# Install Redis
brew install redis

# Start Redis server
brew services start redis

# To stop Redis later (when done testing)
brew services stop redis
```

#### Option B: Using Docker (All platforms)
```bash
# Pull and run Redis in a container
docker run -d --name redis-server -p 6379:6379 redis:latest

# To stop Redis later
docker stop redis-server
```

#### Option C: Download Redis directly (Manual)
1. Go to https://redis.io/download
2. Download Redis for your platform
3. Follow installation instructions
4. Start with: `redis-server`

### 2. Verify Redis is Running
```bash
# Test Redis connection
redis-cli ping
# Should return: PONG
```

### 3. Set Up Postman Environment Variables

#### Step 1: Create Environment
1. In Postman, click the **gear icon** ⚙️ (top right)
2. Click **"Add"** to create new environment
3. Name it: **"JWT Auth Local"**

#### Step 2: Add Variables
Add these variables with their values:

| Variable Name | Initial Value | Current Value |
|--------------|---------------|---------------|
| `userAuthBaseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `productBaseUrl` | `http://localhost:8081` | `http://localhost:8081` |
| `jwtToken` | (leave empty) | (leave empty) |
| `adminToken` | (leave empty) | (leave empty) |
| `userId` | (leave empty) | (leave empty) |

#### Step 3: Select Environment
1. Click the dropdown in top right (should show "No Environment")
2. Select **"JWT Auth Local"**

### 4. Start Your Services

#### Terminal 1 - Start UserAuthService
```bash
cd /Users/gitanj/Work/Software/EcomPlatform/UserAuthService
./mvnw spring-boot:run
```
Wait for: `Started UserAuthServiceApplication in X seconds`

#### Terminal 2 - Start ProductService
```bash
cd /Users/gitanj/Work/Software/EcomPlatform/ProductService
./mvnw spring-boot:run
```
Wait for: `Started ProductServiceApplication in X seconds`

## 🧪 Testing Workflow

### Step 1: Basic Authentication Test
1. Run **"1. User Registration"** - Should get 201 status
2. Run **"2. User Login (Get JWT Token)"** - Should get 200 status and populate `jwtToken`
3. Run **"5. Token Validation"** - Should get 200 status

### Step 2: Admin Setup (Manual Intervention Required!)

After running **"3. Admin Registration"**, you need to manually update the user's role:

#### Option A: Using MySQL Command Line
```sql
-- Connect to your database
mysql -u gitanjsheth -p userauthproj

-- Find the admin user ID
SELECT id, username, email FROM users WHERE email = 'admin@example.com';

-- Find the ADMIN role ID
SELECT id, role_name FROM roles WHERE role_name = 'ADMIN';

-- Add ADMIN role to the admin user (replace USER_ID and ADMIN_ROLE_ID)
INSERT INTO user_roles (user_id, role_id) VALUES (USER_ID, ADMIN_ROLE_ID);

-- Remove USER role if you want admin to only have ADMIN role
DELETE FROM user_roles WHERE user_id = USER_ID AND role_id = (SELECT id FROM roles WHERE role_name = 'USER');
```

#### Option B: Using Database GUI (phpMyAdmin, MySQL Workbench, etc.)
1. Open your database management tool
2. Navigate to `userauthproj` database
3. Go to `user_roles` table
4. Add a new row with the admin user's ID and the ADMIN role ID

### Step 3: Continue Testing
1. Run **"4. Admin Login"** - Should populate `adminToken`
2. Run all ProductService tests to verify authentication works

## 🔍 Troubleshooting

### Redis Issues
```bash
# Check if Redis is running
ps aux | grep redis

# Check Redis logs
tail -f /usr/local/var/log/redis.log

# Test Redis connection
redis-cli ping
```

### Service Issues
```bash
# Check if services are running
lsof -i :8080  # UserAuthService
lsof -i :8081  # ProductService

# Check service logs for errors
# Look at terminal output where you started the services
```

### Common Postman Issues

#### Environment Variables Not Working
- Make sure you selected the right environment (top-right dropdown)
- Variables should show `{{variableName}}` in requests
- Check Variables tab in environment settings

#### Tests Failing
- Check the **Test Results** tab after running a request
- Verify service logs for errors
- Ensure Redis is running for logout/blacklist tests

#### Token Not Being Saved
- Check the **Tests** tab of login requests
- Look for `pm.environment.set("jwtToken", ...)` in test scripts
- Verify environment is selected

## 📋 Test Execution Order

**Required Order** (some tests depend on previous ones):
1. User Registration
2. User Login ← *Sets jwtToken*
3. Admin Registration  
4. **Manual Admin Role Update** ← *Manual step!*
5. Admin Login ← *Sets adminToken*
6. All other tests can run in any order

## 🎯 Expected Results

### Successful Setup Indicators:
✅ Redis responds to `ping` with `PONG`  
✅ Both services start without errors  
✅ User registration returns 201  
✅ User login returns 200 with token  
✅ Token validation returns 200  
✅ Environment variables are populated  

### Common Status Codes:
- **200 OK**: Successful requests
- **201 Created**: Successful registration
- **401 Unauthorized**: Invalid credentials or expired token
- **403 Forbidden**: Valid token but insufficient permissions

## 🛠️ Manual Interventions Summary

1. **Install and start Redis** (one-time setup)
2. **Create Postman environment** (one-time setup)
3. **Start both services** (each testing session)
4. **Manually update admin role in database** (after admin registration)

That's it! Once these are done, you can test your JWT authentication system comprehensively! 🎉 