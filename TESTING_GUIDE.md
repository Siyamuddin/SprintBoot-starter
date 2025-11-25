# Testing Guide - Comprehensive Application Changes

This guide will help you verify that all the new changes work properly.

## Prerequisites

1. **Set Required Environment Variables**
   ```bash
   export JWT_SECRET="your-super-secret-jwt-key-at-least-32-characters-long-for-hs512"
   export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/saas_app?createDatabaseIfNotExist=true"
   export SPRING_DATASOURCE_USERNAME="root"
   export SPRING_DATASOURCE_PASSWORD="your-password"
   ```

2. **Start Required Services**
   - MySQL database
   - Redis (for caching and rate limiting)

## Step 1: Build and Start the Application

```bash
# Build the application
mvn clean install

# Start the application
mvn spring-boot:run
```

**Expected Result:**
- Application starts without errors
- You should see: "Environment variables validated successfully."
- If JWT_SECRET is missing, application should fail with a clear error message

## Step 2: Verify Environment Variable Validation

### Test 1: Missing JWT_SECRET
```bash
# Unset JWT_SECRET
unset JWT_SECRET

# Try to start the application
mvn spring-boot:run
```

**Expected Result:**
- Application should fail to start
- Error message: "CRITICAL ERROR: Required environment variable 'JWT_SECRET' is not set"

### Test 2: Valid Environment Variables
```bash
# Set JWT_SECRET
export JWT_SECRET="your-super-secret-jwt-key-at-least-32-characters-long-for-hs512"

# Start application
mvn spring-boot:run
```

**Expected Result:**
- Application starts successfully
- Log shows: "Environment variables validated successfully."

## Step 3: Test Error Code System

### Test 1: Invalid Login (Should return error code)
```bash
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@example.com",
    "password": "wrongpassword"
  }'
```

**Expected Response:**
```json
{
  "message": "Invalid username or password. Please check your credentials and try again.",
  "success": false,
  "timestamp": "2024-01-01T12:00:00",
  "errorCode": "AUTH_1001"
}
```

### Test 2: Resource Not Found (Should return error code)
```bash
curl -X GET http://localhost:9090/api/v1/users/99999 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "message": "User not found with ID: 99999",
  "success": false,
  "timestamp": "2024-01-01T12:00:00",
  "errorCode": "GEN_9002"
}
```

## Step 4: Test Micrometer Metrics

### Test 1: Access Prometheus Metrics Endpoint
```bash
curl http://localhost:9090/actuator/prometheus
```

**Expected Result:**
- Should return Prometheus-formatted metrics
- Look for metrics like:
  - `app_auth_login_attempts_total{type="success"}` 
  - `app_auth_login_attempts_total{type="failure"}`
  - `app_auth_registrations_total`
  - `app_sessions_active`
  - `app_accounts_locked`

### Test 2: Access Metrics Endpoint
```bash
curl http://localhost:9090/actuator/metrics
```

**Expected Result:**
- Returns list of available metrics
- Should include custom metrics like `app.auth.login.attempts`, `app.auth.registrations`, etc.

### Test 3: Get Specific Metric
```bash
curl http://localhost:9090/actuator/metrics/app.auth.login.attempts
```

**Expected Result:**
- Returns detailed information about login attempts metric
- Shows measurements with tags (type=success, type=failure)

### Test 4: Verify Metrics are Being Recorded

1. **Perform some operations:**
```bash
# Register a user
curl -X POST http://localhost:9090/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Test123!@#"
  }'

# Try to login (will fail if email not verified)
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#"
  }'
```

2. **Check metrics again:**
```bash
curl http://localhost:9090/actuator/metrics/app.auth.registrations
curl http://localhost:9090/actuator/metrics/app.auth.login.attempts
```

**Expected Result:**
- Registration counter should be > 0
- Login attempts counter should be > 0

## Step 5: Test Security Improvements

### Test 1: Email Verification Check on Login
```bash
# Register a user
curl -X POST http://localhost:9090/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test2@example.com",
    "password": "Test123!@#"
  }'

# Try to login before email verification
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2@example.com",
    "password": "Test123!@#"
  }'
```

**Expected Result:**
- Should return 401 Unauthorized
- Error message: "Email not verified. Please verify your email before logging in."
- Error code: "AUTH_1002"

### Test 2: Password Validation in Change Password
```bash
# Login first (get token)
TOKEN=$(curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "verified@example.com",
    "password": "Test123!@#"
  }' | jq -r '.jwtToken')

# Try to change password with weak password
curl -X POST "http://localhost:9090/api/v1/users/me/change-password?currentPassword=Test123!@#&newPassword=weak" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Result:**
- Should return 400 Bad Request
- Error message about password requirements
- Error code should be present

### Test 3: Password Reset Information Disclosure Fix
```bash
# Request password reset for non-existent email
curl -X POST "http://localhost:9090/api/v1/auth/forgot-password?email=nonexistent@example.com"
```

**Expected Result:**
- Should return 200 OK (not 404 or error)
- Message: "Password reset email sent if account exists"
- This prevents email enumeration attacks

## Step 6: Test Rate Limiting

### Test 1: Rate Limit on Login
```bash
# Try to login multiple times rapidly
for i in {1..15}; do
  curl -X POST http://localhost:9090/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "email": "test@example.com",
      "password": "wrongpassword"
    }'
  echo ""
done
```

**Expected Result:**
- After 10 attempts (default limit), should return 429 Too Many Requests
- Response should include "Retry-After" header
- Error code: "RATE_6001"

## Step 7: Test Request ID Tracking

### Test 1: Check Request ID Header
```bash
curl -v http://localhost:9090/api/v1/users \
  -H "Authorization: Bearer YOUR_TOKEN" \
  2>&1 | grep -i "x-request-id"
```

**Expected Result:**
- Response should include `X-Request-ID` header
- Value should be a UUID
- Should also appear in logs

## Step 8: Test Health Endpoints

### Test 1: Health Check
```bash
curl http://localhost:9090/actuator/health
```

**Expected Result:**
- Should return health status
- Should include database and Redis health checks

### Test 2: Health Details (if authenticated)
```bash
curl http://localhost:9090/actuator/health \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Result:**
- Should return detailed health information
- Shows status of all components

## Step 9: Test Pagination

### Test 1: Get All Users with Pagination
```bash
curl "http://localhost:9090/api/v1/users?pageNumber=0&pageSize=10&sortBy=id&sortDirec=asc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Result:**
- Should return `PagedResponse` with:
  - `content`: Array of users
  - `pageNumber`: Current page
  - `pageSize`: Page size
  - `totalElements`: Total number of users
  - `totalPages`: Total number of pages
  - `lastPage`: Boolean indicating if last page

## Step 10: Verify Logs

### Check Application Logs
Look for:
1. **Request IDs** in log messages
2. **Environment validation** messages
3. **Metrics** being recorded
4. **Security events** being logged
5. **No hardcoded secrets** in logs

```bash
# Watch logs in real-time
tail -f logs/application.log
```

## Step 11: Integration Test Script

Create a comprehensive test script:

```bash
#!/bin/bash

BASE_URL="http://localhost:9090/api/v1"
JWT_SECRET="test-jwt-secret-at-least-32-characters-long"

echo "=== Testing Environment Validation ==="
# Test will fail if JWT_SECRET not set
echo "✓ Environment validation should pass if JWT_SECRET is set"

echo ""
echo "=== Testing Registration ==="
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Integration Test User",
    "email": "integration@test.com",
    "password": "Test123!@#"
  }')
echo "Registration Response: $REGISTER_RESPONSE"

echo ""
echo "=== Testing Metrics ==="
METRICS=$(curl -s "$BASE_URL/../actuator/metrics/app.auth.registrations")
echo "Registration Metrics: $METRICS"

echo ""
echo "=== Testing Error Codes ==="
ERROR_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@test.com",
    "password": "wrong"
  }')
echo "Error Response: $ERROR_RESPONSE"
echo "Should contain 'errorCode' field"

echo ""
echo "=== Testing Health Endpoint ==="
HEALTH=$(curl -s "$BASE_URL/../actuator/health")
echo "Health: $HEALTH"

echo ""
echo "=== All Tests Completed ==="
```

## Step 12: Manual Verification Checklist

- [ ] Application starts without errors
- [ ] Environment variable validation works
- [ ] Error codes are returned in API responses
- [ ] Prometheus metrics endpoint is accessible
- [ ] Custom metrics are being recorded
- [ ] Email verification check works on login
- [ ] Password validation works in change password
- [ ] Password reset doesn't leak email existence
- [ ] Rate limiting works on sensitive endpoints
- [ ] Request IDs are present in responses and logs
- [ ] Health endpoints work correctly
- [ ] Pagination returns proper metadata
- [ ] No hardcoded secrets in code or logs

## Troubleshooting

### Application Won't Start
- Check if JWT_SECRET is set: `echo $JWT_SECRET`
- Check database connection
- Check Redis connection
- Review application logs for specific errors

### Metrics Not Appearing
- Verify Prometheus is enabled in `application.properties`
- Check if `/actuator/prometheus` endpoint is accessible
- Ensure operations are being performed to generate metrics
- Check application logs for metric registration errors

### Error Codes Not Showing
- Verify `ApiResponse` includes `errorCode` field
- Check `GlobalExceptionHandler` is using `ErrorCode` enum
- Ensure controllers are using updated `ApiResponse` constructors

### Rate Limiting Not Working
- Verify Redis is running
- Check rate limit configuration in `application.properties`
- Review `RateLimitInterceptor` logs

## Next Steps

1. Set up Prometheus and Grafana for metrics visualization
2. Configure alerting based on metrics
3. Set up log aggregation (ELK stack, Splunk, etc.)
4. Create automated integration tests
5. Set up CI/CD pipeline with these tests

