## NotificationService

Sends emails for order, payment, and user events. Consumes Kafka topics, manages templates, and provides admin APIs. Integrates with AWS SES (SDK) for email delivery.

### Tech Stack
- Java 17, Spring Boot 3.2.0 (compatible)
- Spring Web, Spring Security, Spring Data JPA (MySQL)
- Spring Kafka
- AWS SES SDK

### Service Port
- Default: `8085`

### Prerequisites
- MySQL at `jdbc:mysql://localhost:3306/notificationserviceproj`
- Kafka at `localhost:9092`
- AWS SES credentials (`aws.ses.*`) for real delivery; in dev, use test keys.

### Configuration
- DB/JPA: `spring.datasource.*`, `spring.jpa.*`
- Kafka: `spring.kafka.*`
- SES: `aws.ses.region`, `aws.ses.access-key`, `aws.ses.secret-key`, `aws.ses.from-email`, `aws.ses.reply-to`
- Security: `app.security.jwt.*`, `app.service.token` (for internal route if used)

### Security
- Public: `/api/notifications/health`, `/api/notifications/public/**`
- Authenticated: `/api/notifications/**` (ADMIN for write operations)
- Internal send endpoint `/api/notifications/internal/send` allowed by web security but requires `X-Service-Token` header.

### Endpoints (selection)
- Templates (ADMIN):
  - POST `/api/notifications/templates`
  - PUT `/api/notifications/templates/{templateId}`
  - GET `/api/notifications/templates/{templateName}`
  - GET `/api/notifications/templates/type/{notificationType}`
- Notifications (ADMIN):
  - POST `/api/notifications` → create
  - POST `/api/notifications/{notificationId}/send` → send
- Internal:
  - POST `/api/notifications/internal/send` (requires `X-Service-Token`)

### Kafka Consumers
- `order.events`, `payment.events`, `user.events` → build template data and create notifications.

### Build & Run
```
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/notification-service-*.jar
```

### Design & Implementation Notes
- Requirements: email notifications for key domain events with templating.
- Design: template CRUD + default initializer; Kafka listeners per domain; SES adapter for delivery.
- Security: JWT for user/admin; internal token for service triggered sends.


