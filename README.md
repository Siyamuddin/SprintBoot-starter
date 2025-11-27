# Developer Guideline - SAAS Starter Kit

## Table of Contents
1. [Overview](#overview)
2. [Application Architecture](#application-architecture)
3. [Getting Started - Step by Step](#getting-started---step-by-step)
4. [Configuration Guide](#configuration-guide)
5. [How the Application Works](#how-the-application-works)
6. [Important Things to Keep in Mind](#important-things-to-keep-in-mind)
7. [Extending and Customizing](#extending-and-customizing)
8. [Development Best Practices](#development-best-practices)
9. [Troubleshooting](#troubleshooting)

---

## Overview

This is a **production-ready Spring Boot 3 SAAS starter kit** that provides a complete foundation for building multi-tenant or single-tenant SAAS applications. It includes user management, authentication, security, file uploads, rate limiting, caching, metrics, and observability features out of the box.

### Technology Stack
- **Framework**: Spring Boot 3.2.1
- **Java Version**: 17
- **Build Tool**: Maven
- **Database**: MySQL 8.0 (with Flyway migrations)
- **Cache**: Redis (optional, for caching and rate limiting)
- **Security**: Spring Security + JWT
- **Documentation**: OpenAPI 3 / Swagger
- **Metrics**: Micrometer + Prometheus
- **File Storage**: Local filesystem or AWS S3

### Key Features
- ✅ JWT Authentication with refresh tokens
- ✅ User registration and email verification
- ✅ Password reset functionality
- ✅ Account lockout protection
- ✅ Rate limiting (per-IP and per-user)
- ✅ File uploads (local or S3)
- ✅ Session management
- ✅ Audit logging
- ✅ CORS configuration
- ✅ API documentation (Swagger)
- ✅ Health checks and metrics
- ✅ Docker support

---

## Application Architecture

### Package Structure

```
com.siyamuddin.blog.blogappapis/
├── Config/              # Configuration classes
│   ├── Properties/      # Configuration property classes
│   ├── SecurityConfig   # Spring Security configuration
│   ├── CacheConfig      # Redis caching configuration
│   ├── MetricsConfig    # Micrometer metrics
│   └── ...
├── Controllers/         # REST API endpoints
│   ├── AuthController   # Authentication endpoints
│   └── UserController   # User management endpoints
├── Entity/              # JPA entities (database models)
│   ├── User             # User entity
│   ├── Role             # Role entity
│   ├── RefreshToken     # Refresh token entity
│   └── ...
├── Repository/          # Data access layer (Spring Data JPA)
├── Services/            # Business logic layer
│   ├── Impl/            # Service implementations
│   └── Storage/         # File storage abstractions
├── Security/            # Security-related classes
│   ├── JwtHelper        # JWT token generation/validation
│   ├── JwtAuthenticationFilter  # JWT filter
│   └── RateLimitInterceptor     # Rate limiting
├── Exceptions/          # Custom exceptions and global handler
└── Payloads/            # DTOs and request/response models
```

### Data Flow

1. **Request** → `RateLimitInterceptor` (checks rate limits)
2. → `JwtAuthenticationFilter` (validates JWT if authenticated)
3. → `Controller` (handles HTTP request)
4. → `Service` (business logic)
5. → `Repository` (database operations)
6. → **Response** (with proper status codes and error handling)

### Database Schema

The application uses **Flyway** for database migrations:
- `V1__init_roles.sql` - Creates role table and seeds admin/normal roles
- `V2__add_profile_photo_columns.sql` - Adds profile image columns

Key tables:
- `user` - User accounts with email, password, profile info
- `role` - User roles (ROLE_ADMIN, ROLE_NORMAL)
- `user_role` - Many-to-many relationship
- `refresh_token` - Refresh tokens for JWT rotation
- `user_session` - Active user sessions
- `token_blacklist` - Invalidated tokens
- `audit_log` - Security and user action audit trail

---

## Getting Started - Step by Step

### Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use included `mvnw`)
- MySQL 8.0+ (or use Docker Compose)
- Redis (optional, for caching and rate limiting)
- Docker & Docker Compose (optional, for easy setup)

### Step 1: Clone and Explore

```bash
# Clone the repository
git clone <repository-url>
cd saas-starter

# Explore the structure
ls -la
```

### Step 2: Configure Environment Variables

**IMPORTANT**: The application requires a `JWT_SECRET` environment variable.

```bash
# Copy the example environment file
cp env.example .env

# Edit .env and set your values
# CRITICAL: Set JWT_SECRET to a random string at least 32 characters long
nano .env
```

**Required Environment Variables:**
- `JWT_SECRET` - **REQUIRED** - Must be at least 32 characters (used for JWT signing)
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

**Optional but Recommended:**
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `APP_EMAIL_FROM` - Email sender address
- `APP_CORS_ALLOWED_ORIGINS` - Comma-separated list of allowed CORS origins

### Step 3: Set Up Database

#### Option A: Using Docker Compose (Recommended for Development)

```bash
# Start MySQL and Redis
docker compose up -d db redis

# Wait for MySQL to be healthy (check logs)
docker compose logs -f db

# Database will be created automatically by Flyway migrations
```

#### Option B: Using Local MySQL

```bash
# Create database manually
mysql -u root -p
CREATE DATABASE saas_app;
exit

# Update .env or application-dev.properties with your connection details
```

### Step 4: Configure Application Properties

The application uses **Spring profiles**:
- `dev` - Development profile (default)
- `prod` - Production profile

**Key configuration files:**
- `application.properties` - Base configuration (shared)
- `application-dev.properties` - Development overrides
- `application-prod.properties` - Production overrides

**Development defaults** (`application-dev.properties`):
- Database: `jdbc:mysql://localhost:3306/saas_app`
- Hibernate DDL: `update` (auto-updates schema)
- Debug logging enabled

**Production** (set via environment variables):
- Hibernate DDL: Should be `validate` or `none`
- Proper JWT secret (32+ characters)
- Real SMTP configuration
- CORS origins configured

### Step 5: Build the Application

```bash
# Clean and build
./mvnw clean install

# Or on Windows
mvnw.cmd clean install
```

### Step 6: Run the Application

#### Option A: Using Maven

```bash
# Run with default profile (dev)
./mvnw spring-boot:run

# Or run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Option B: Using Docker Compose (Full Stack)

```bash
# Start everything (MySQL, Redis, Application)
docker compose up --build

# View logs
docker compose logs -f app

# Stop everything
docker compose down
```

#### Option C: Using JAR File

```bash
# Build JAR
./mvnw clean package

# Run JAR
java -jar target/saas-starter-0.0.1-SNAPSHOT.jar

# Or with profile
java -jar -Dspring.profiles.active=prod target/saas-starter-0.0.1-SNAPSHOT.jar
```

### Step 7: Verify Installation

1. **Check health endpoint:**
   ```bash
   curl http://localhost:9090/actuator/health
   ```

2. **Access Swagger UI:**
   Open browser: `http://localhost:9090/swagger-ui/index.html`

3. **Check application logs** for:
   - Database connection success
   - Flyway migrations applied
   - Environment validation passed
   - No critical errors

### Step 8: Create Your First User

Use Swagger UI or curl:

```bash
# Register a new user
curl -X POST http://localhost:9090/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "about": "Test user"
  }'
```

**Note:** In development, check logs for email verification token (if SMTP not configured, emails won't send but tokens are generated).

---

## Configuration Guide

### Critical Configuration

#### 1. JWT Secret (REQUIRED)

**Why it matters:** Used to sign and verify JWT tokens. If compromised, attackers can forge tokens.

```bash
# Generate a secure secret (32+ characters)
openssl rand -base64 32

# Set in environment
export JWT_SECRET="your-generated-secret-here"
```

**Or in `.env` file:**
```env
APP_JWT_SECRET=your-generated-secret-here
```

#### 2. Database Configuration

**Development:**
```properties
# application-dev.properties
spring.datasource.url=jdbc:mysql://localhost:3306/saas_app?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

**Production (via environment variables):**
```env
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/saas_app
SPRING_DATASOURCE_USERNAME=dbuser
SPRING_DATASOURCE_PASSWORD=secure-password
```

**IMPORTANT:** In production, set `spring.jpa.hibernate.ddl-auto=validate` or `none` and rely only on Flyway migrations.

#### 3. Redis Configuration (Optional)

**If caching is enabled:**
```properties
# application.properties
app.caching.enabled=true
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=  # Optional
```

**Disable caching (no Redis needed):**
```properties
app.caching.enabled=false
```

#### 4. Email Configuration

**Development (MailHog or similar):**
```properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
```

**Production (SMTP provider):**
```env
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_TLS=true
```

#### 5. CORS Configuration

**Development:**
```env
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

**Production:**
```env
APP_CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

#### 6. File Storage Configuration

**Local Storage (Default):**
```properties
filestorage.mode=local
filestorage.local.base-path=uploads
filestorage.local.public-uri-prefix=/uploads
```

**S3 Storage:**
```properties
filestorage.mode=s3
filestorage.s3.bucket-name=your-bucket-name
filestorage.s3.region=us-east-1
filestorage.s3.access-key=${AWS_ACCESS_KEY_ID}
filestorage.s3.secret-key=${AWS_SECRET_ACCESS_KEY}
filestorage.s3.public-base-url=https://cdn.yourdomain.com
```

### Configuration Properties Reference

| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `JWT_SECRET` | Secret for JWT signing | - | ✅ Yes |
| `app.jwt.access-token-validity` | Access token expiry (seconds) | 900 (15 min) | No |
| `app.jwt.refresh-token-validity` | Refresh token expiry (seconds) | 604800 (7 days) | No |
| `app.security.max-failed-login-attempts` | Failed attempts before lockout | 5 | No |
| `app.security.account-lockout-duration-minutes` | Lockout duration | 30 | No |
| `app.rate-limit.login.requests` | Login requests per duration | 10 | No |
| `app.rate-limit.login.duration` | Duration in hours | 1 | No |
| `app.caching.enabled` | Enable Redis caching | true | No |
| `server.port` | Application port | 9090 | No |

See `application.properties` for complete list.

---

## How the Application Works

### Authentication Flow

1. **Registration:**
   ```
   POST /api/v1/auth/register
   → UserService.registerNewUser()
   → EmailVerificationService sends verification email
   → User created with emailVerified=false
   ```

2. **Email Verification:**
   ```
   POST /api/v1/auth/verify-email?token=xxx
   → EmailVerificationService.verifyEmail()
   → User.emailVerified = true
   ```

3. **Login:**
   ```
   POST /api/v1/auth/login
   → RateLimitInterceptor checks per-IP limit
   → AuthenticationManager validates credentials
   → AccountSecurityService checks if locked
   → JwtHelper generates access + refresh tokens
   → SessionService creates session
   → Returns JWT tokens
   ```

4. **Accessing Protected Endpoints:**
   ```
   GET /api/v1/users/me
   → JwtAuthenticationFilter extracts token from Authorization header
   → JwtHelper validates token
   → Sets SecurityContext with UserDetails
   → Controller processes request
   ```

5. **Token Refresh:**
   ```
   POST /api/v1/auth/refresh-token?refreshToken=xxx
   → Validates refresh token from database
   → Generates new access token
   → Returns new access token (same refresh token)
   ```

6. **Logout:**
   ```
   POST /api/v1/auth/logout
   → TokenBlacklistService blacklists access token
   → SessionService invalidates all sessions
   → RefreshTokenRepo revokes all refresh tokens
   ```

### Rate Limiting System

**How it works:**
- Uses **Bucket4j** library for token bucket algorithm
- Stores buckets in **Redis** (distributed) or in-memory (single instance)
- Applied via `RateLimitInterceptor` before controller execution

**Rate limit types:**
- **Per-IP** (unauthenticated): Login, registration, password reset
- **Per-User** (authenticated): Post creation, comments, password change
- **General API**: All authenticated endpoints (default: 50,000/hour)

**Configuration:**
```properties
app.rate-limit.login.requests=10
app.rate-limit.login.duration=1  # hours
```

**Response headers when rate limited:**
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Remaining requests
- `X-RateLimit-Reset`: Time until reset (seconds)

### File Storage System

**Storage Modes:**

1. **Local Mode** (`filestorage.mode=local`):
   - Files stored in `uploads/` directory
   - Served via `/uploads/**` endpoint
   - Configurable base path

2. **S3 Mode** (`filestorage.mode=s3`):
   - Files uploaded to AWS S3
   - Returns public URL or CDN URL
   - Supports cleanup of old files

**Usage Example:**
```java
// Profile photo upload automatically uses configured storage
POST /api/v1/users/me/profile-photo
Content-Type: multipart/form-data
file: [binary]
```

**Storage abstraction:**
- `FileStorageService` interface
- `LocalFileStorageService` implementation
- `S3FileStorageService` implementation
- Easy to add new backends (Azure, GCS, etc.)

### Caching System

**When enabled:**
- Redis must be running
- `app.caching.enabled=true`

**Cached entities:**
- User lookups (to reduce database queries)
- Configurable TTL per cache

**Adding cache:**
```java
@Cacheable(value = "users", key = "#id")
public UserDto getUserById(Integer id) {
    // ...
}
```

### Metrics and Observability

**Available metrics:**

1. **Business Metrics** (custom):
   - `app.auth.login.attempts` - Login attempts (total/success/failure)
   - `app.auth.registrations` - User registrations
   - `app.auth.password.reset.*` - Password reset events
   - `app.sessions.active` - Current active sessions
   - `app.accounts.locked` - Currently locked accounts

2. **HTTP Metrics** (automatic):
   - Request counts, durations, errors
   - Available at `/actuator/metrics`

3. **Prometheus Endpoint:**
   - `/actuator/prometheus` - Exports metrics in Prometheus format

**Viewing metrics:**
```bash
# Prometheus format
curl http://localhost:9090/actuator/prometheus

# JSON format
curl http://localhost:9090/actuator/metrics
```

### Security Features

1. **Account Lockout:**
   - After N failed login attempts, account locks
   - Configurable duration (default: 30 minutes)
   - Automatic unlock after duration expires

2. **Password Validation:**
   - Minimum length (default: 8)
   - Requires uppercase, lowercase, digit, special character
   - Configurable via `app.security.password-*` properties

3. **Token Blacklisting:**
   - Logged-out tokens stored in blacklist
   - Prevents reuse of old tokens
   - Cleaned up via scheduled task

4. **Session Management:**
   - Track active sessions per user
   - Revoke sessions individually or all at once
   - Session timeout configurable

---

## Important Things to Keep in Mind

### 🔴 Critical Security Considerations

1. **JWT_SECRET:**
   - **MUST** be at least 32 characters
   - **MUST** be kept secret (never commit to git)
   - **MUST** be different in dev/prod environments
   - Generate using: `openssl rand -base64 32`

2. **Database Passwords:**
   - Never hardcode in source code
   - Use environment variables or secrets management
   - Use strong passwords in production

3. **Email Verification:**
   - Configure `app.security.require-email-verification-for-login=true` in production
   - Ensure SMTP is properly configured
   - Test email delivery before going live

4. **CORS Configuration:**
   - **CRITICAL** in production: Set `APP_CORS_ALLOWED_ORIGINS` to your actual frontend domains
   - Don't use wildcards (`*`) in production
   - Test CORS with actual frontend application

5. **Rate Limiting:**
   - Default limits are per-application-instance (if using in-memory)
   - For distributed systems, ensure Redis is configured
   - Adjust limits based on your use case

### 🟡 Production Readiness Checklist

- [ ] JWT_SECRET set and secure (32+ chars)
- [ ] Database credentials secure and not in code
- [ ] Hibernate DDL set to `validate` or `none` (rely on Flyway)
- [ ] SMTP configured for email delivery
- [ ] CORS origins configured for your frontend
- [ ] File storage configured (S3 recommended for production)
- [ ] Redis configured if using caching/rate limiting
- [ ] Logging configured appropriately (not DEBUG in prod)
- [ ] Health checks monitored
- [ ] Backup strategy for database
- [ ] SSL/HTTPS configured (recommended)

### 🟢 Development Best Practices

1. **Use Environment Variables:**
   - Never hardcode secrets
   - Use `.env` file (add to `.gitignore`)
   - Document required variables in `env.example`

2. **Database Migrations:**
   - Always use Flyway migrations for schema changes
   - Never modify existing migrations (create new ones)
   - Test migrations on a copy of production data

3. **Testing:**
   - Run tests before committing: `./mvnw test`
   - Write tests for new features
   - Test security-critical paths manually

4. **Code Organization:**
   - Keep controllers thin (delegate to services)
   - Use DTOs for request/response (don't expose entities)
   - Follow package structure conventions

5. **Error Handling:**
   - Use `GlobalExceptionHandler` for consistent error responses
   - Include error codes from `ErrorCode` enum
   - Log errors appropriately (not stack traces in response)

### 🟠 Common Pitfalls

1. **Forgetting JWT_SECRET:**
   - Application will fail to start
   - Error in logs: "JWT_SECRET environment variable is required"

2. **Database Connection Issues:**
   - Check MySQL is running
   - Verify connection URL, username, password
   - Check network connectivity

3. **Redis Connection Issues:**
   - If caching enabled but Redis down, application may fail
   - Set `app.caching.enabled=false` to disable temporarily

4. **Email Not Sending:**
   - Check SMTP configuration
   - Verify `app.email.enabled=true`
   - Check application logs for SMTP errors

5. **CORS Errors:**
   - Browser shows CORS errors if origins not configured
   - Check `APP_CORS_ALLOWED_ORIGINS` environment variable
   - Verify frontend URL matches configured origins

6. **File Upload Issues:**
   - Check file size limits (`spring.servlet.multipart.max-file-size`)
   - Verify storage path exists and is writable (local mode)
   - Check S3 credentials and bucket permissions (S3 mode)

---

## Extending and Customizing

### Adding a New Entity

1. **Create Entity Class:**
```java
@Entity
@Table(name = "your_table")
@Getter
@Setter
public class YourEntity extends BaseEntity {
    // fields with JPA annotations
}
```

2. **Create Repository:**
```java
@Repository
public interface YourEntityRepository extends JpaRepository<YourEntity, Integer> {
    // custom query methods
}
```

3. **Create DTO:**
```java
public class YourEntityDto {
    // fields matching your needs
}
```

4. **Create Service:**
```java
@Service
public class YourEntityService {
    // business logic
}
```

5. **Create Controller:**
```java
@RestController
@RequestMapping("/api/v1/your-entities")
public class YourEntityController {
    // endpoints
}
```

6. **Add Migration:**
   - Create `V3__create_your_table.sql` in `src/main/resources/db/migration/`

### Adding a New Endpoint

1. Add method to appropriate Controller
2. Use `@PreAuthorize` for authorization if needed
3. Add rate limiting if required (via `RateLimitInterceptor`)
4. Document with OpenAPI annotations (`@Operation`, `@ApiResponses`)
5. Test via Swagger UI

### Adding Custom Rate Limits

1. Add property to `RateLimitProperties`:
```java
private RateLimitConfig yourFeature = new RateLimitConfig(10, 1);
```

2. Add method to `RateLimitService`:
```java
public boolean tryConsumeYourFeature(String identifier) {
    return getBucket("rate-limit:your-feature:" + identifier, 
                     rateLimitProperties.getYourFeature())
            .tryConsume(1);
}
```

3. Add check in `RateLimitInterceptor`:
```java
if (isYourFeatureEndpoint(requestURI, method)) {
    ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingYourFeature(identifier);
    // handle rate limit
}
```

### Adding a New Storage Backend

1. Implement `FileStorageService` interface:
```java
@Service
@ConditionalOnProperty(name = "filestorage.mode", havingValue = "your-backend")
public class YourStorageService implements FileStorageService {
    // implement store() and delete() methods
}
```

2. Update `FileStorageProperties.StorageMode` enum if needed

3. Configure properties for your backend

### Customizing Security

**Changing Password Requirements:**
- Modify `app.security.password-*` properties
- Update `PasswordValidationService` if needed

**Changing Lockout Policy:**
- Modify `app.security.max-failed-login-attempts`
- Modify `app.security.account-lockout-duration-minutes`

**Adding Custom Roles:**
- Create Flyway migration to insert new roles
- Update `RoleProperties` if needed

### Adding Business Logic

1. Create service interface and implementation in `Services/` and `Services/Impl/`
2. Inject dependencies via constructor
3. Use repositories for data access
4. Add logging for important operations
5. Add metrics if it's a key business metric
6. Add audit logging for security-sensitive operations

---

## Development Best Practices

### Code Style

1. **Use Lombok** (already included):
   - `@Getter`, `@Setter` for entities
   - `@RequiredArgsConstructor` for constructors
   - `@Slf4j` for logging

2. **Follow Spring Boot Conventions:**
   - Controllers return `ResponseEntity<T>`
   - Use `@Valid` for request validation
   - Use DTOs, not entities, in API responses

3. **Error Handling:**
   - Use custom exceptions from `Exceptions/` package
   - Let `GlobalExceptionHandler` handle mapping to HTTP responses
   - Include meaningful error messages

### Testing

**Run tests:**
```bash
./mvnw test
```

**Write unit tests:**
- Service layer: Mock repositories
- Controller layer: Mock services
- Security: Test authentication/authorization

**Integration tests:**
- Test complete flows (register → verify → login)
- Use `@SpringBootTest` annotation

### Logging

**Log levels:**
- `DEBUG`: Detailed information for debugging (dev only)
- `INFO`: General application flow
- `WARN`: Potential issues
- `ERROR`: Error conditions

**Security events:**
- Use `SecurityEventLogger` for security-related logs
- Use `AuditService` for audit trail

### Version Control

1. **Never commit:**
   - `.env` files
   - Secrets or passwords
   - Build artifacts (`target/`)
   - IDE files (unless team-wide)

2. **Always commit:**
   - Database migrations
   - Configuration examples (`env.example`)
   - Documentation updates

---

## Troubleshooting

### Application Won't Start

**Problem:** Environment validation fails
```
Error: JWT_SECRET environment variable is required
```

**Solution:**
- Set `JWT_SECRET` environment variable (32+ characters)
- Check `.env` file is in project root
- Verify `EnvironmentValidator` logs for specific issues

**Problem:** Database connection fails
```
Error: Unable to connect to database
```

**Solution:**
- Verify MySQL is running: `docker compose ps` or `mysql -u root -p`
- Check connection URL, username, password
- Ensure database exists (or `createDatabaseIfNotExist=true` is set)
- Check firewall/network connectivity

### Rate Limiting Not Working

**Problem:** Rate limits not enforced

**Solution:**
- Verify `RateLimitInterceptor` is registered (check `WebConfig`)
- Check Redis is running if using distributed rate limiting
- Review application logs for rate limit messages
- Verify endpoint is not excluded from interceptor

### Email Not Sending

**Problem:** No verification emails received

**Solution:**
- Check `app.email.enabled=true`
- Verify SMTP configuration (`spring.mail.*` properties)
- Check application logs for SMTP errors
- Test SMTP connection separately
- In development, check MailHog or similar tool

### CORS Errors in Browser

**Problem:** `Access-Control-Allow-Origin` error

**Solution:**
- Set `APP_CORS_ALLOWED_ORIGINS` environment variable
- Include your frontend URL (e.g., `http://localhost:3000`)
- Restart application after changing CORS config
- Check browser console for exact error

### File Upload Fails

**Problem:** File upload returns error

**Solution:**
- Check file size doesn't exceed `spring.servlet.multipart.max-file-size`
- Verify storage directory exists and is writable (local mode)
- Check S3 credentials and bucket permissions (S3 mode)
- Review application logs for specific error

### Performance Issues

**Problem:** Slow response times

**Solution:**
- Enable caching (`app.caching.enabled=true`) and ensure Redis is running
- Check database query performance (enable SQL logging in dev)
- Review rate limiting impact (too restrictive?)
- Check connection pool settings (`spring.datasource.hikari.*`)

---

## Additional Resources

### Documentation Files

- `README.md` - Overview and quick start (this file)

### External Documentation

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [JWT.io](https://jwt.io/) - JWT token decoder/debugger
- [Flyway Documentation](https://flywaydb.org/documentation/)

### Getting Help

1. Check application logs first
2. Review this guideline and GUIDELINE.md
3. Check Swagger UI for API documentation
4. Review test files for usage examples
5. Check GitHub issues (if applicable)

---

## Quick Reference

### Common Commands

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Test
./mvnw test

# Docker Compose
docker compose up -d          # Start services
docker compose logs -f app    # View logs
docker compose down           # Stop services

# Database
docker compose exec db mysql -u root -p
```

### Key Endpoints

- Swagger UI: `http://localhost:9090/swagger-ui/index.html`
- Health: `http://localhost:9090/actuator/health`
- Metrics: `http://localhost:9090/actuator/metrics`
- Prometheus: `http://localhost:9090/actuator/prometheus`
- API Base: `http://localhost:9090/api/v1/`

### Important File Locations

- Config: `src/main/resources/application*.properties`
- Migrations: `src/main/resources/db/migration/`
- Environment: `.env` (create from `env.example`)
- Logs: Check console output or Docker logs

---

**Last Updated:** 2025/11
**Maintained By:** Development UDDIN SIYAM

For questions or improvements to this guide, please open an issue or submit a pull request.
