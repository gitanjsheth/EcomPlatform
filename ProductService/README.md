## ProductService

Manages products, categories, and inventory for the platform. Provides public product APIs, internal inventory operations, and Elasticsearch-backed search with autocomplete and analytics.

### Tech Stack
- Java 24, Spring Boot 3.5.3
- Spring Web, Spring Security, Spring Data JPA (MySQL), Flyway
- Spring Data Elasticsearch (ES 8.x)
- Lombok, Validation

### Service Port
- Default: `8081`

### Prerequisites
- MySQL at `jdbc:mysql://localhost:3306/productserviceproj`
- Elasticsearch at `http://localhost:9200`
- UserAuthService for JWT validation endpoint `POST /auth/validate-token`

### Configuration
`src/main/resources/application.properties`
- DB: `spring.datasource.*`, `spring.jpa.*`, `spring.flyway.*`
- Auth service: `app.auth-service.url`
- Elasticsearch: `spring.elasticsearch.uris`
- Internal service token: `app.service.token` or env `INTERNAL_SERVICE_TOKEN`

### Security
- Public GETs for `/products`, `/products/{id}`, `/products/{id}/availability`, `/categories/**`, `/search/**`.
- Admin restricted for product/category mutations.
- Internal inventory endpoints and cart validation rely on `X-Service-Token` at controller layer.

### Key Endpoints
- Products
  - GET `/products` | `/products/` | `/products/{id}`
  - POST `/products/` (ADMIN), PUT `/products/{id}` (ADMIN), DELETE/PATCH for hard/soft delete (ADMIN)
  - GET `/products/{id}/availability` (public)

- Inventory (internal)
  - POST `/products/{id}/inventory/reserve|release|confirm` (requires `X-Service-Token`)
  - POST `/products/validate-cart` → `{ items: [{ productId, quantity }] }` → `{ valid, errors? }`

- Search
  - GET `/search?q=...&page=&size=` → ES match on title/description
  - GET `/search/autocomplete?q=...&size=` → prefix on title
  - GET `/admin/search-analytics` (ADMIN)
  - POST `/admin/search/reindex` (ADMIN)

### Search & Analytics
- Index model `ProductSearchDocument` stored in index `products`.
- Indexing on create/update/delete product.
- Query analytics logged with execution time, result count, UA/IP.

### Build & Run
```
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/ProductService-*.jar
```

### Interactions
- CartService uses `/products/{id}/availability` and `/products/validate-cart`.
- OrderManagementService calls inventory reserve/release/confirm for each item.

### Design & Implementation Notes
- Requirements: CRUD, soft-delete, inventory management, search & autocomplete.
- Design: JPA entities with soft-delete (`@SQLDelete`, `@Where`), ES index for search, analytics entity for observability.
- Security: Public read endpoints, ADMIN-protected mutations, internal endpoints guarded by service token.


