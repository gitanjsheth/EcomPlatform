# PaymentService

Handles payment processing simulation and webhooks. Provides internal endpoints for OrderManagementService to process payments and initiate refunds. **NEW: Includes comprehensive payment receipt generation and retrieval functionality.**

## Features

- **Payment Processing**: Secure payment handling with multiple gateway support
- **Webhook Management**: Payment gateway webhook processing
- **Refund Processing**: Automated and manual refund handling
- **Receipt Generation**: Professional payment receipts in multiple formats
- **Event-Driven**: Kafka integration for payment events
- **Multi-Gateway Support**: Stripe SDK integration (prepared)
- **Payment Analytics**: Transaction tracking and reporting

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 24
- **Database**: MySQL 8.0+ (Spring Data JPA)
- **Message Broker**: Apache Kafka 3.x
- **Payment Gateway**: Stripe SDK
- **Security**: JWT Authentication
- **Build Tool**: Maven

## Prerequisites

- Java 24+
- MySQL 8.0+
- Apache Kafka 3.x
- Maven 3.6+

## Configuration

### Application Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/paymentserviceproj
spring.datasource.username=gitanjsheth
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.group-id=payment-service

# Payment Gateway Configuration
app.payment.gateway.api-key=${STRIPE_API_KEY:sk_test_...}
app.payment.gateway.secret-key=${STRIPE_SECRET_KEY:sk_test_...}
app.payment.gateway.webhook-secret=${STRIPE_WEBHOOK_SECRET:whsec_...}

# Internal Service Token
app.service.token=${INTERNAL_SERVICE_TOKEN:internal-service-secret-2024}

# Receipt Configuration
app.receipt.default-currency=USD
app.receipt.company-name=E-Commerce Platform
app.receipt.company-address=123 Business St, City, State 12345
app.receipt.support-email=support@ecommerce.com
```

## Setup Instructions

### 1. Database Setup

```sql
CREATE DATABASE paymentserviceproj;
CREATE USER 'gitanjsheth'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON paymentserviceproj.* TO 'gitanjsheth'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Kafka Setup

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 3. Stripe Setup (Optional for Development)

```bash
# Install Stripe CLI for webhook testing
# Download from: https://stripe.com/docs/stripe-cli

# Forward webhooks to local service
stripe listen --forward-to localhost:8084/api/payments/webhook
```

### 4. Application Startup

```bash
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/PaymentService-*.jar
```

## API Endpoints

### Payment Processing (Internal)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/payments/process` | Process payment for order | Service Token |
| `POST` | `/api/payments/{paymentId}/refund` | Initiate refund for payment | Service Token |

### Payment Receipt Functionality (Public)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/api/payments/{paymentId}/receipt` | Get payment receipt in HTML format | Public |
| `GET` | `/api/payments/{paymentId}/receipt` | Get payment receipt in plain text | Public (Accept: text/plain) |
| `GET` | `/api/payments/{paymentId}/receipt-url` | Get receipt URL and available formats | Public |
| `POST` | `/api/payments/{paymentId}/generate-receipt` | Generate receipt for payment | Service Token |

### Webhook Processing (Public)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/payments/webhook` | Process payment gateway webhooks | Public |

## Receipt Features

- ✅ **Automatic receipt generation** for successful payments
- ✅ **Multiple formats**: HTML (styled), Plain Text
- ✅ **Receipt validation**: Only generated for COMPLETED payments
- ✅ **Professional styling**: Clean, printable receipt design
- ✅ **Payment method details**: Card information (masked)
- ✅ **Transaction details**: Amount, currency, timestamps
- ✅ **Receipt metadata**: Unique receipt IDs and URLs

## Event Integration

### Kafka Topics
- `payment.events`: Payment lifecycle events

### Event Types
- `PAYMENT_COMPLETED`: Payment processed successfully
- `PAYMENT_FAILED`: Payment processing failed
- `REFUND_INITIATED`: Refund process started
- `REFUND_COMPLETED`: Refund processed successfully

## Security Model

- **Public Endpoints**: Receipt retrieval and webhook processing
- **Internal Endpoints**: Require `X-Service-Token` header
- **Webhook Security**: Stripe signature verification when present
- **Stateless Design**: No server-side sessions

## Build & Run

```bash
# Development
./mvnw -DskipTests spring-boot:run

# Production
./mvnw -DskipTests package && java -jar target/PaymentService-*.jar
```

## Service Port

- **Default**: `8084`

## Design & Implementation Notes

- **Requirements**: Payment processing, webhook handling, receipt generation
- **Design**: Kafka consumer listens to `order.events` (CREATED) for payment triggers
- **Security**: Controller-enforced service token for internal calls, webhook signature verification
- **Extensibility**: Support for multiple payment gateways, advanced fraud detection, and subscription billing

## Monitoring & Health

- **Health Endpoint**: `/actuator/health`
- **Metrics**: Prometheus-compatible metrics
- **Logging**: Structured logging with SLF4J
- **Performance**: Payment processing time and success rates

## Receipt Implementation Details

- **ReceiptService**: Handles receipt generation, formatting, and validation
- **Automatic generation**: Receipts are generated automatically when payments complete successfully
- **Format support**: HTML (styled) and plain text formats
- **Security**: Receipt endpoints are public but validate payment status
- **Extensibility**: Designed to support PDF generation and cloud storage in future
- **Metadata storage**: Receipt URLs stored in payment metadata for easy retrieval


