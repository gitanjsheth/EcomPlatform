## PaymentService

Handles payment processing simulation and webhooks. Provides internal endpoints for OrderManagementService to process payments and initiate refunds.

### Tech Stack
- Java 24, Spring Boot 3.5.3
- Spring Web, Spring Security, Spring Data JPA (MySQL)
- Stripe SDK (prepared), Kafka (optional in future)

### Service Port
- Default: `8084`

### Prerequisites
- MySQL at `jdbc:mysql://localhost:3306/paymentserviceproj`

### Configuration
- DB/JPA: `spring.datasource.*`, `spring.jpa.*`
- Integration: `app.order-service.url` (not used directly here), `app.service.token`

### Security
- Stateless; internal endpoints permitted but require `X-Service-Token` at controller layer.

### Endpoints
- POST `/api/payments/process` (internal): body `{ orderId, amount, currency }` → status COMPLETED if amount < 1000
- POST `/api/payments/{paymentId}/refund` (internal): `{ amount, reason }` → status REFUND_INITIATED
- POST `/api/payments/webhook` (public): accept gateway callbacks (mock)

### Build & Run
```
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/PaymentService-*.jar
```

### Design & Implementation Notes
- Requirements: basic payment flow integration with orders.
- Design: controller-enforced service token; extensible for real gateways.
- Security: service-to-service trust via `X-Service-Token`.


