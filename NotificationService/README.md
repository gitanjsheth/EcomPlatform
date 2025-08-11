# NotificationService

Sends emails for order, payment, and user events. Consumes Kafka topics, manages templates, and provides admin APIs. Integrates with AWS SES (SDK) for email delivery.

## Features

- **Email Notifications**: Automated email delivery for key platform events
- **Template Management**: Dynamic email templates with customization
- **Multi-Event Support**: Order, payment, and user event processing
- **AWS SES Integration**: Professional email delivery service
- **Event-Driven**: Kafka integration for real-time notifications
- **Admin APIs**: Template and notification management
- **User Preferences**: Configurable notification settings

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0+ (Spring Data JPA)
- **Message Broker**: Apache Kafka 3.x
- **Email Service**: AWS SES SDK
- **Security**: JWT Authentication
- **Build Tool**: Maven

## Prerequisites

- Java 17+
- MySQL 8.0+
- Apache Kafka 3.x
- AWS SES Account
- Maven 3.6+

## Configuration

### Application Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/notificationserviceproj
spring.datasource.username=gitanjsheth
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

# AWS SES Configuration
aws.ses.region=${AWS_SES_REGION:us-east-1}
aws.ses.access-key=${AWS_SES_ACCESS_KEY:your-access-key}
aws.ses.secret-key=${AWS_SES_SECRET_KEY:your-secret-key}
aws.ses.from-email=${AWS_SES_FROM_EMAIL:noreply@yourdomain.com}
aws.ses.reply-to=${AWS_SES_REPLY_TO:support@yourdomain.com}

# Security Configuration
app.security.jwt.secret=${JWT_SECRET:defaultSecretForDevelopment}
app.service.token=${INTERNAL_SERVICE_TOKEN:internal-service-secret-2024}

# Notification Configuration
app.notification.default-language=en
app.notification.max-retry-attempts=3
app.notification.retry-delay-minutes=5
```

## Setup Instructions

### 1. Database Setup

```sql
CREATE DATABASE notificationserviceproj;
CREATE USER 'gitanjsheth'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON notificationserviceproj.* TO 'gitanjsheth'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Kafka Setup

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 3. AWS SES Setup

```bash
# Configure AWS CLI
aws configure

# Verify SES access
aws ses get-send-quota --region us-east-1

# Set up verified email addresses
aws ses verify-email-identity --email-address noreply@yourdomain.com --region us-east-1
```

### 4. Application Startup

```bash
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/notification-service-*.jar
```

## API Endpoints

### Notification Management (ADMIN)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/notifications` | Create notification | Admin Role |
| `POST` | `/api/notifications/{notificationId}/send` | Send notification | Admin Role |
| `GET` | `/api/notifications/user/{userId}` | Get user's notifications | User/Admin |
| `GET` | `/api/notifications/status/{status}` | Get notifications by status | Admin Role |

### Template Management (ADMIN)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/notifications/templates` | Create template | Admin Role |
| `PUT` | `/api/notifications/templates/{templateId}` | Update template | Admin Role |
| `GET` | `/api/notifications/templates/{templateName}` | Get template by name | Admin Role |
| `GET` | `/api/notifications/templates/type/{notificationType}` | Get templates by type | Admin Role |
| `POST` | `/api/notifications/templates/initialize` | Initialize default templates | Admin Role |

### Internal Service Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/notifications/internal/send` | Send notification (internal) | Service Token |

## Event Integration

### Kafka Topics
- `order.events`: Order lifecycle events
- `payment.events`: Payment processing events
- `user.events`: User account events

### Event Types
- `ORDER_CREATED`: New order notification
- `ORDER_STATUS_UPDATED`: Order status change notification
- `PAYMENT_COMPLETED`: Payment success notification
- `PAYMENT_FAILED`: Payment failure notification
- `USER_REGISTERED`: Welcome email
- `PASSWORD_RESET_REQUESTED`: Password reset email

## Security Model

- **Public Endpoints**: `/api/notifications/health`
- **Authenticated Endpoints**: Most notification operations require valid JWT
- **Admin Endpoints**: Template and notification management require `ROLE_ADMIN`
- **Internal Endpoints**: Service-to-service communication with `X-Service-Token`

## Build & Run

```bash
# Development
./mvnw -DskipTests spring-boot:run

# Production
./mvnw -DskipTests package && java -jar target/notification-service-*.jar
```

## Service Port

- **Default**: `8085`

## Design & Implementation Notes

- **Requirements**: Email notifications for key domain events with templating
- **Design**: Template CRUD + default initializer, Kafka listeners per domain, SES adapter for delivery
- **Security**: JWT for user/admin, internal token for service triggered sends
- **Extensibility**: Support for SMS, push notifications, and multi-language templates

## Monitoring & Health

- **Health Endpoint**: `/actuator/health`
- **Metrics**: Prometheus-compatible metrics
- **Logging**: Structured logging with SLF4J
- **Performance**: Email delivery success rates and processing times

## Template System

The service includes a comprehensive template management system:

- **Default Templates**: Pre-configured templates for common notifications
- **Template Variables**: Dynamic content substitution
- **Multi-Format Support**: HTML and plain text email formats
- **Template Versioning**: Track template changes and updates
- **Localization Ready**: Support for multiple languages and regions


