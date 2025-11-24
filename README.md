# SAAS Spring Boot Starter Kit

Production-ready Spring Boot 3 starter focused on user management, authentication, and security. Use it as a base for new SAAS ideas without rebuilding account infrastructure each time.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Key Features

- **JWT Authentication + Refresh Tokens** with blacklist + session tracking
- **Advanced User Management**: email verification, password reset, account lockout, audit logging
- **Rate Limiting** using Bucket4j + interceptor coverage
- **Email Service** abstractions (welcome, verification, reset, lockout)
- **Flyway Migrations** with seed data
- **Testing Suite** covering JWT helper + account security service
- **Swagger/OpenAPI 3** documentation at `/swagger-ui/index.html`
- **Configuration Profiles** for dev/prod, fully externalized properties
- **Scalability Hooks**: Redis caching ready (see below), async processors, scheduled cleanup jobs

## Getting Started

1. **Install dependencies**
   ```bash
   ./mvnw clean install
   ```
2. **Configure environment**
   - Copy `application-dev.properties` to match your DB credentials
   - Set env vars (`JWT_SECRET`, DB creds, SMTP, Redis) as needed
3. **Run migrations + app**
   ```bash
   ./mvnw spring-boot:run
   ```

## Development Workflow

### Local iteration loop
1. **Configure secrets** – Duplicate `env.example` to `.env`, populate secrets/passwords, and align `application-*.properties` with your local DB.
2. **Database ready** – Start MySQL (Docker compose or local service) and let Flyway bootstrap schema.
3. **Optional caching** – Start Redis if `app.caching.enabled=true`; otherwise leave it off.
4. **Run the app** – `./mvnw spring-boot:run` (or your IDE). Keep the console open for security logs/rate-limit traces.
5. **Execute tests** – `./mvnw test` before pushing.
6. **Iterate** – Adjust code, rerun migrations/tests, repeat.

### Deployment flow
1. Build artifact: `./mvnw clean package`.
2. Provide production `.env` (JWT secret, DB, SMTP, Redis, toggles).
3. Apply Flyway migrations on boot (or run `./mvnw flyway:migrate` pre-deploy).
4. Deploy jar/docker image to your platform (Docker/Kubernetes/VM) and point ingress at port `9090`.
5. Monitor logs + health endpoints (`/actuator/health`, `/actuator/info`) for readiness.

### Run with Docker Compose

1. Copy `env.example` to `.env` and adjust credentials/secrets.
   ```bash
   cp env.example .env
   ```
2. Build images and start services:
   ```bash
   docker compose up --build
   ```
   - The compose file waits for MySQL to report healthy before booting the app.
3. API runs on `http://localhost:9090`, MySQL on `localhost:${DATABASE_PORT_MAPPING}` (defaults to 3307 to avoid conflicts with a local MySQL), Redis on `localhost:${REDIS_PORT}` (per `.env`).
4. Stop everything:
   ```bash
   docker compose down
   ```

## Configuration Reference

| Property | Description |
| --- | --- |
| `app.jwt.*` | Token secrets + expiry overrides |
| `app.email.*` | Sender details + verification/reset URLs |
| `app.security.*` | Password + lockout policies |
| `app.rate-limit.*` | Endpoint throttling controls |
| `app.caching.enabled` | Master switch to turn Redis caching on/off |
| `spring.flyway.*` | Migration toggles, locations |
| `spring.data.redis.*` | Redis host/port/password (enable caching) |

See `application.properties` for defaults.

## Database Migrations (Flyway)

- `db/migration/V1__init_roles.sql` creates the base `role` table + seeds admin/normal roles.
- Hibernate remains in `update` mode for rapid iteration; switch to `validate` in production.
- Add future schema changes as new `V2__*.sql` files.

## Testing

- `JwtHelperTest` ensures token generation + validation.
- `AccountSecurityServiceImplTest` covers lockout + reset behavior.
- Run suite via `./mvnw test`.

## Scalability Hooks

- **Redis caching ready** (toggle with `app.caching.enabled` and provide Redis props)
- `@EnableAsync` + task executor configured for email/audit offloading
- Scheduled cleanup jobs for sessions + token blacklist run hourly

## User Guide

### Account lifecycle
1. **Register** via `POST /api/v1/auth/register` with email/password. User receives verification email if SMTP is configured.
2. **Verify email** using the link generated with `app.email.verification-base-url`. Verification flips the user to active status.
3. **Authenticate** with `POST /api/v1/auth/login`. Successful logins yield an access token, refresh token, and session record.
4. **Use protected APIs** by passing the access token in the `Authorization: Bearer <token>` header.
5. **Refresh tokens** through `POST /api/v1/auth/refresh` before the access token expires. Old refresh tokens are invalidated and can be blacklisted on logout.
6. **Logout / rotate credentials** by calling `POST /api/v1/auth/logout` (invalidates session + refresh token).

### Account recovery & security
- **Forgot password**: `POST /api/v1/auth/request-reset` issues a reset email using `app.email.password-reset-base-url`. Complete with `POST /api/v1/auth/reset-password`.
- **Failed login handling**: After `app.security.max-failed-login-attempts` the account locks for `app.security.account-lockout-duration-minutes`.
- **Session tracking**: Sessions persist in the DB so administrators can audit active devices and revoke them if needed.

### Rate limits at a glance
- `app.rate-limit.login.*`, `registration.*`, and `general.*` throttle sensitive endpoints.
- Exceeding a bucket throws `RateLimitExceededException`, surfaced as HTTP 429 with details in the response payload and logs.

## Administrator / Operator Notes

- **Role seeding**: Flyway migration `V1__init_roles.sql` inserts baseline roles. Extend with new migrations for extra permissions.
- **Auditing**: Critical authentication events are logged via `AuditLog` entities and can be extended for compliance needs.
- **Observability**: Logging categories (see `application.properties`) highlight security, rate limiting, and general app behavior. Tune levels per environment.
- **Feature toggles**: Email notifications (`app.email.enabled`) and caching (`app.caching.enabled`) can be flipped at runtime via properties or env vars for quick troubleshooting.

## API Documentation

- Swagger UI: `http://localhost:9090/swagger-ui/index.html`
- Auth endpoints live under `/api/v1/auth/**`
- User endpoints live under `/api/v1/users/**`

## Next Steps

- Plug in a real SMTP provider (SES, SendGrid, etc.)
- Add module-specific domains (billing, project management, etc.)
- Extend tests with controller/integration coverage as features grow
