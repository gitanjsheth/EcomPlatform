## UserAuthService

Authentication and user management microservice. Issues and validates JWTs, manages users/roles, supports token blacklist via Redis, and exposes token validation for other services.

### Tech Stack
- Java 24, Spring Boot 3.5.3
- Spring Web, Spring Security, Spring Data JPA (MySQL), Flyway
- Redis (token blacklist)
- JJWT 0.12.x

### Service Port
- Default: `8080`

### Prerequisites
- MySQL at `jdbc:mysql://localhost:3306/userauthproj`
- Redis at `localhost:6379`

### Configuration
- DB/JPA: `spring.datasource.*`, `spring.jpa.*`, `spring.flyway.enabled`
- JWT: `app.jwt.secret` (base64-encoded), expiration settings
- Redis: `spring.data.redis.*`

### Security
- Public `/auth/**` + health/actuator; stateless.

### Endpoints
- POST `/auth/signup` → create user
- POST `/auth/login` → returns `{ user, token, expiresAt }`
- POST `/auth/validate-token` (used by other services) → `{ valid, user, message }` (Authorization: Bearer <token>)
- POST `/auth/logout` → blacklists token
- POST `/auth/admin/users/{userId}/ban` → bans and invalidates tokens (extend with ADMIN guard in gateway)

### Build & Run
```
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/UserAuthService-*.jar
```

### Design & Implementation Notes
- Requirements: signup/login/logout, token validation for SSO among services.
- Design: JPA models for users/roles; JWT contains userId, username, roles; Redis blacklist with JTI TTL.
- Security: Stateless; other services call `/auth/validate-token` for verification.


