## CartService

Robust cart management microservice for the e-commerce platform. Manages user and guest carts, supports inventory-aware operations, Redis-based caching, and MongoDB persistence. Designed to integrate seamlessly with ProductService and OrderManagementService.

### Tech Stack
- Java 24, Spring Boot 3.5.3
- Spring Web, Spring Security
- MongoDB (Spring Data MongoDB)
- Redis (Spring Data Redis)
- Lombok, Validation

### Service Port
- Default: `8082`

### Prerequisites
- Java 21+ (project targeted at 24) and Maven (wrapper included)
- MongoDB running at `mongodb://localhost:27017/cartservice`
- Redis at `localhost:6379`
- UserAuthService at `http://localhost:8080` (for JWT validation used by other services; CartService trusts JWT injected by edge or other services)
- ProductService at `http://localhost:8081`

### Configuration
Key properties (`src/main/resources/application.properties`):
- MongoDB: `spring.data.mongodb.uri`
- Redis: `spring.data.redis.*`
- Service URLs: `app.auth-service.url`, `app.product-service.url`
- Security/dev: `app.security.jwt.secret` (for validation in development), `INTERNAL_SERVICE_TOKEN` (env) or `app.service.token` (used by other services when calling internal endpoints)
- Cart behavior: `app.cart.*`

Recommended environment variable for inter-service calls:
```
INTERNAL_SERVICE_TOKEN=internal-service-secret-2024
```

### Security Model
- End-user cart endpoints require JWT (set by the API gateway/edge). Guest flows allowed via session ID header.
- Internal endpoints (read-only cart by ID, checkout marker) require header `X-Service-Token` that matches `app.service.token` or `INTERNAL_SERVICE_TOKEN`.

### Main Endpoints
- Public (authenticated end-user):
  - GET `/api/carts` → current cart by user/session
  - POST `/api/carts/add` → add to cart
  - PUT `/api/carts/items/{productId}` → update quantity
  - DELETE `/api/carts/items/{productId}` → remove item
  - DELETE `/api/carts/clear` → clear cart
  - POST `/api/carts/merge?sessionId=...` → merge guest cart into user cart
  - GET `/api/carts/count` → item count
  - GET `/api/carts/contains/{productId}` → product presence

- Internal (service-to-service):
  - GET `/api/carts/{cartId}` (requires `X-Service-Token`)
  - POST `/api/carts/{cartId}/checkout` (requires `X-Service-Token`) → mark cart as checked out

Headers commonly used:
```
Authorization: Bearer <JWT>
X-Session-ID: <guest-session-id>
X-Service-Token: <internal-service-token>
```

### Inventory Awareness
- On reads and modifications, CartService queries ProductService:
  - `/products/{id}/availability` to verify availability and quantities.
- Cached availability in Redis for 10 minutes.

### Build & Run
```
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/CartService-*.jar
```

### Interactions Overview (Platform Flow)
1. User/guest builds a cart here.
2. OrderManagementService fetches cart by ID (internal) and creates order.
3. Cart marked as checked out internally by OrderManagementService.

### Design & Implementation Notes
- Requirements: Guest + authenticated carts, merge flow, inventory-aware checks.
- Design: MongoDB document model with TTL index on `expiresAt`; Redis cache for carts and product availability.
- Security: JWT for end-user routes; `X-Service-Token` for internal routes; CORS restricted.
- Extensibility: Add rate limiting, item-level metadata, and promotions as future work.


