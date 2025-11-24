<!-- 6526307d-4081-43ff-bdf3-bda71c6ebf1f 5950f52b-27dc-4e18-bce2-70c92e466f7c -->
# SAAS Spring Boot Starter Kit Conversion Plan

## Overview

Transform the current blog application into a reusable SAAS starter kit with advanced user management, clean architecture, and best practices for rapid development of new SAAS applications. No need of 2 factorauthentication for now.

## Phase 1: Core Architecture Refactoring

### 1.1 Package Structure Reorganization

- **Current**: `com.siyamuddin.blog.blogappapis`
- **New**: `com.saas.starter` or `com.yourcompany.saas`
- Create modular package structure:
  ```
  ├── core/
  │   ├── config/
  │   ├── security/
  │   ├── exception/
  │   └── common/
  ├── modules/
  │   └── user/
  │       ├── controller/
  │       ├── service/
  │       ├── repository/
  │       ├── entity/
  │       └── dto/
  └── shared/
      ├── utils/
      └── constants/
  ```


### 1.2 Remove Blog-Specific Features

- Delete `PostController`, `PostService`, `PostServiceImpl`, `PostRepo`, `Post` entity
- Delete `CommentController`, `CommentService`, `CommentServiceImpl`, `CommentRepo`, `Comment` entity
- Delete `CategoryController`, `CategoryService`, `CategoryServiceImpl`, `CategoryRepo`, `Category` entity
- Remove blog-related DTOs from `PostPayloads/` package
- Clean up references in `AuthorizationService` (remove `canModifyPost`, `canModifyComment`)
- Update `SecurityConfig` to remove blog-related public URLs

### 1.3 Base Entity Pattern

- Create `BaseEntity` with common fields:
  - `id` (UUID or Long)
  - `createdAt`, `updatedAt` (audit fields)
  - `createdBy`, `updatedBy` (user tracking)
  - `version` (optimistic locking)
- All entities extend `BaseEntity`

## Phase 2: Advanced User Management

### 2.1 Enhanced User Entity

- Add fields:
  - `emailVerified` (boolean)
  - `emailVerificationToken` (String)
  - `emailVerificationTokenExpiry` (Date)
  - `passwordResetToken` (String)
  - `passwordResetTokenExpiry` (Date)
  - `failedLoginAttempts` (int)
  - `accountLockedUntil` (Date)
  - `lastLoginDate` (Date)
  - `profileImageUrl` (String)
  - `phoneNumber` (String, optional)
  - `timezone` (String)
  - `locale` (String)

### 2.2 User Service Enhancements

- **Email Verification Service**:
  - `sendVerificationEmail(User user)`
  - `verifyEmail(String token)`
  - `resendVerificationEmail(String email)`
- **Password Reset Service**:
  - `requestPasswordReset(String email)`
  - `resetPassword(String token, String newPassword)`
  - `validateResetToken(String token)`
- **Account Security Service**:
  - `lockAccount(String email, int durationMinutes)`
  - `unlockAccount(String email)`
  - `incrementFailedLoginAttempts(String email)`
  - `resetFailedLoginAttempts(String email)`
- **2FA Service**:
  - `enable2FA(Integer userId)`
  - `disable2FA(Integer userId)`
  - `verify2FACode(Integer userId, String code)`
  - `generateQRCode(Integer userId)`
- **OAuth Integration Service**:
  - Support Google, GitHub, Microsoft OAuth
  - `linkOAuthAccount(User user, String provider, String providerId)`
  - `unlinkOAuthAccount(User user, String provider)`

### 2.3 Session Management

- Create `UserSession` entity:
  - `sessionId` (UUID)
  - `userId` (FK to User)
  - `ipAddress`, `userAgent`
  - `loginTime`, `lastActivity`
  - `expiresAt`
  - `isActive`
- Session service:
  - `createSession(User user, HttpServletRequest)`
  - `invalidateSession(String sessionId)`
  - `invalidateAllUserSessions(Integer userId)`
  - `getActiveSessions(Integer userId)`
  - `refreshSession(String sessionId)`

### 2.4 Audit Logging

- Create `AuditLog` entity:
  - `userId`, `action`, `resourceType`, `resourceId`
  - `ipAddress`, `userAgent`, `timestamp`
  - `success`, `errorMessage` (optional)
- Audit service:
  - `logUserAction(User user, String action, String resourceType, Object resourceId)`
  - `logSecurityEvent(User user, String event, boolean success)`
  - Query methods for audit trail

## Phase 3: Security Enhancements

### 3.1 JWT Improvements

- Add refresh token mechanism:
  - `JwtTokenPair` (access + refresh tokens)
  - `refreshToken(String refreshToken)` endpoint
  - Store refresh tokens in database with expiry
- Token revocation:
  - `TokenBlacklist` entity/service
  - Check blacklist in `JwtAuthenticationFilter`
- Configurable token expiry (access: 15min, refresh: 7 days)

### 3.2 Enhanced Security Config

- Add password policy configuration:
  - Min length, complexity requirements
  - Password history (prevent reuse)
- Account lockout policy:
  - Max failed attempts, lockout duration
- Session timeout configuration
- CSRF protection for state-changing operations

### 3.3 Rate Limiting Improvements

- Per-user rate limiting (not just global)
- Rate limit storage in Redis (for distributed systems)
- Different limits for different user roles
- Rate limit headers in responses

## Phase 4: API Structure & Documentation

### 4.1 RESTful API Best Practices

- Standardize response format:
  - `ApiResponse<T>` wrapper with `data`, `message`, `status`, `timestamp`
  - Pagination response wrapper
- Version API endpoints: `/api/v1/`
- Consistent error codes and messages
- API versioning strategy

### 4.2 Enhanced Swagger Documentation

- Complete OpenAPI 3.0 documentation
- Security scheme documentation
- Request/response examples
- Error response documentation
- API grouping by modules

### 4.3 User Controller Enhancements

- `GET /api/v1/users/me` - Get current user profile
- `PUT /api/v1/users/me` - Update own profile
- `POST /api/v1/users/me/change-password` - Change password
- `POST /api/v1/users/me/enable-2fa` - Enable 2FA
- `POST /api/v1/users/me/verify-2fa` - Verify 2FA setup
- `GET /api/v1/users/me/sessions` - Get active sessions
- `DELETE /api/v1/users/me/sessions/{sessionId}` - Revoke session
- `POST /api/v1/auth/verify-email` - Verify email
- `POST /api/v1/auth/resend-verification` - Resend verification
- `POST /api/v1/auth/forgot-password` - Request password reset
- `POST /api/v1/auth/reset-password` - Reset password
- `POST /api/v1/auth/refresh-token` - Refresh access token
- `POST /api/v1/auth/logout` - Logout (revoke token)

## Phase 5: Configuration & Environment

### 5.1 Externalized Configuration

- Move all hardcoded values to `application.properties`
- Environment-specific configs:
  - `application-dev.properties`
  - `application-staging.properties`
  - `application-prod.properties`
- Configuration properties classes:
  - `JwtProperties`
  - `EmailProperties`
  - `SecurityProperties`
  - `RateLimitProperties`

### 5.2 Feature Flags

- Create feature flag system:
  - `FeatureFlag` entity/service
  - Enable/disable features per environment
  - Examples: `EMAIL_VERIFICATION_ENABLED`, `2FA_ENABLED`, `OAUTH_ENABLED`

## Phase 6: Email Service

### 6.1 Email Service Implementation

- Create `EmailService` interface and implementation
- Support multiple providers (SMTP, SendGrid, AWS SES)
- Email templates:
  - Welcome email
  - Email verification
  - Password reset
  - Account locked notification
  - 2FA setup instructions
- Async email sending (use `@Async`)

## Phase 7: Database & Persistence

### 7.1 Database Migrations

- Add Flyway or Liquibase for schema versioning
- Initial migration scripts
- Seed data for roles, default admin user

### 7.2 Query Optimization

- Add database indexes:
  - `email` (unique)
  - `emailVerificationToken`
  - `passwordResetToken`
  - `createdAt`, `updatedAt`
- Use `@Query` annotations for complex queries
- Implement soft delete pattern (optional)

## Phase 8: Testing Infrastructure

### 8.1 Test Structure

- Unit tests for services
- Integration tests for controllers
- Security test utilities
- Test data builders
- Mock email service for tests

## Phase 9: Documentation & Examples

### 9.1 README Updates

- Project overview
- Setup instructions
- Configuration guide
- API documentation link
- Architecture overview
- How to add new modules

### 9.2 Code Examples

- Example module structure
- How to add new features
- How to extend user management
- Integration examples

## Phase 10: Scalability Considerations

### 10.1 Caching Strategy

- Redis integration for:
  - Session storage
  - Rate limiting
  - Token blacklist
  - User cache
- Cache configuration classes

### 10.2 Async Processing

- Spring `@Async` for:
  - Email sending
  - Audit logging
  - Heavy computations
- Task executor configuration

### 10.3 Database Connection Pooling

- HikariCP configuration
- Connection pool tuning
- Read/write replica support (optional)

## Implementation Order

1. **Phase 1**: Remove blog features, reorganize structure
2. **Phase 2**: Enhance User entity and core user services
3. **Phase 3**: Security enhancements (JWT refresh, token revocation)
4. **Phase 4**: API structure and documentation
5. **Phase 5**: Configuration externalization
6. **Phase 6**: Email service
7. **Phase 7**: Database migrations
8. **Phase 8**: Testing infrastructure
9. **Phase 9**: Documentation
10. **Phase 10**: Scalability features

## Key Files to Modify/Create

### Delete:

- `PostController.java`, `PostService.java`, `PostServiceImpl.java`, `PostRepo.java`, `Post.java`
- `CommentController.java`, `CommentService.java`, `CommentServiceImpl.java`, `CommentRepo.java`, `Comment.java`
- `CategoryController.java`, `CategoryService.java`, `CategoryServiceImpl.java`, `CategoryRepo.java`, `Category.java`
- `PostPayloads/` package
- Blog-related methods in `AuthorizationService.java`

### Create:

- `BaseEntity.java` - Base entity class
- `UserSession.java` - Session entity
- `AuditLog.java` - Audit log entity
- `TokenBlacklist.java` - Token blacklist entity
- `EmailService.java` - Email service interface/impl
- `EmailVerificationService.java`
- `PasswordResetService.java`
- `TwoFactorService.java`
- `OAuthService.java`
- `SessionService.java`
- `AuditService.java`
- Configuration property classes
- Enhanced DTOs for user management

### Modify:

- `User.java` - Add new fields
- `UserService.java` / `UserServiceImpl.java` - Add new methods
- `UserController.java` - Add new endpoints
- `AuthController.java` - Add email verification, password reset, refresh token
- `SecurityConfig.java` - Update security rules
- `JwtHelper.java` - Add refresh token support
- `JwtAuthenticationFilter.java` - Add token blacklist check
- `AuthorizationService.java` - Remove blog methods, keep user methods
- `GlobalExceptionHandler.java` - Add new exception handlers
- `application.properties` - Externalize all configs