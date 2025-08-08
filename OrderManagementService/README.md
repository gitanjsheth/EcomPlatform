## OrderManagementService

Coordinates the order lifecycle: creation from cart, inventory reservation/confirmation, payment processing, status updates, and event publishing for notifications.

### Tech Stack
- Java 24, Spring Boot 3.5.3
- Spring Web, Spring Security, Spring Data JPA (MySQL), Flyway
- Spring Kafka (event publishing)

### Service Port
- Default: `8083`

### Prerequisites
- MySQL at `jdbc:mysql://localhost:3306/ordermanagementproj`
- Kafka at `localhost:9092`
- Dependent services and base URLs in properties:
  - CartService `app.cart-service.url` (8082)
  - ProductService `app.product-service.url` (8081)
  - PaymentService `app.payment-service.url` (8084)

### Configuration
- DB/JPA: `spring.datasource.*`, `spring.jpa.*`, `spring.flyway.*`
- Kafka: `spring.kafka.*`
- Internal service token: `app.service.token` or env `INTERNAL_SERVICE_TOKEN`

### Security
- Internal endpoints permitted at web layer but must include `X-Service-Token`. Tracking GET endpoints require authentication (via gateway).

### Key Flows
1. Create order from cart:
   - GET cart from CartService (internal)
   - Validate items via ProductService `/products/validate-cart`
   - Create order + reserve inventory (per item)
   - Mark cart as checked out (internal)
   - Publish order created event
2. Payment callbacks (internal): update payment status, confirm/release inventory, publish events
3. Scheduled tasks: auto-cancel old orders; cleanup expired reservations

### Endpoints (selection)
- POST `/api/orders` (internal, requires `X-Service-Token`): create from cart
- GET `/api/orders/user/{userId}` (internal)
- POST `/api/orders/{orderId}/inventory/{reserve|release|confirm}` (internal)
- POST `/api/orders/{orderId}/payment/{completed|failed}` (internal)
- GET `/api/tracking/order/{orderNumber}` (public with auth/phone or internal token)

### Build & Run
```
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/OrderManagementService-*.jar
```

### Design & Implementation Notes
- Requirements: order lifecycle, inventory sync, payment integration, events.
- Design: services for cart/product/payment integrations; events for downstream notification.
- Security: controller-level validation of `X-Service-Token` across internal routes.


