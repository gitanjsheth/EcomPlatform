# OrderManagementService

Coordinates the order lifecycle: creation from cart, inventory reservation/confirmation, payment processing, status updates, and event publishing for notifications.

## Features

- **Order Lifecycle Management**: Complete order processing from cart to delivery
- **Checkout Flow**: User-friendly checkout experience with validation
- **Inventory Integration**: Real-time inventory reservation and confirmation
- **Payment Processing**: Seamless payment integration and callbacks
- **Order Tracking**: Comprehensive order status management
- **Event-Driven**: Kafka integration for order events
- **Multi-Service Integration**: Coordinates Cart, Product, and Payment services

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 24
- **Database**: MySQL 8.0+ (Spring Data JPA, Flyway)
- **Message Broker**: Apache Kafka 3.x
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
spring.datasource.url=jdbc:mysql://localhost:3306/ordermanagementproj
spring.datasource.username=gitanjsheth
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Flyway Configuration
spring.flyway.baseline-on-migrate=true
spring.flyway.enabled=true

# Service URLs
app.cart-service.url=http://localhost:8082
app.product-service.url=http://localhost:8081
app.payment-service.url=http://localhost:8084

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Internal Service Token
app.service.token=${INTERNAL_SERVICE_TOKEN:internal-service-secret-2024}
```

## Setup Instructions

### 1. Database Setup

```sql
CREATE DATABASE ordermanagementproj;
CREATE USER 'gitanjsheth'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ordermanagementproj.* TO 'gitanjsheth'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Kafka Setup

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 3. Application Startup

```bash
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/OrderManagementService-*.jar
```

## API Endpoints

### Public User-Facing Endpoints (Authentication Required)

#### Checkout Flow

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/checkout/initiate?cartId={cartId}` | Start checkout process | JWT Token |
| `GET` | `/api/checkout/summary/{cartId}` | Get checkout summary | JWT Token |
| `POST` | `/api/checkout/validate/{cartId}` | Validate cart for checkout | JWT Token |
| `POST` | `/api/checkout/complete` | Complete checkout and create order | JWT Token |
| `GET` | `/api/checkout/payment-methods` | Get available payment methods | JWT Token |
| `GET` | `/api/checkout/shipping-options?cartId={cartId}` | Get shipping options | JWT Token |

#### Order Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/api/orders/my-orders` | Get current user's orders | JWT Token |
| `GET` | `/api/orders/my-orders/status/{status}` | Get orders by status | JWT Token |
| `GET` | `/api/orders/my-orders/{orderId}` | Get specific order | JWT Token |
| `GET` | `/api/orders/my-orders/number/{orderNumber}` | Get order by number | JWT Token |
| `POST` | `/api/orders/my-orders/{orderId}/cancel` | Cancel order | JWT Token |

### Internal Service Endpoints (Requires X-Service-Token)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/api/orders` | Create order from cart | Service Token |
| `GET` | `/api/orders/user/{userId}` | Get user's orders | Service Token |
| `GET` | `/api/orders/user/{userId}/status/{status}` | Get user's orders by status | Service Token |
| `GET` | `/api/orders/{orderId}/user/{userId}` | Get specific order for user | Service Token |
| `GET` | `/api/orders/number/{orderNumber}/user/{userId}` | Get order by number for user | Service Token |
| `POST` | `/api/orders/{orderId}/cancel` | Cancel order (internal) | Service Token |
| `POST` | `/api/orders/{orderId}/inventory/reserve` | Reserve inventory | Service Token |
| `POST` | `/api/orders/{orderId}/inventory/release` | Release inventory reservation | Service Token |
| `POST` | `/api/orders/{orderId}/inventory/confirm` | Confirm inventory usage | Service Token |
| `POST` | `/api/orders/{orderId}/payment/completed` | Handle payment completion | Service Token |
| `POST` | `/api/orders/{orderId}/payment/failed` | Handle payment failure | Service Token |
| `POST` | `/api/orders/{orderId}/ship` | Mark order as shipped | Service Token |
| `POST` | `/api/orders/{orderId}/deliver` | Mark order as delivered | Service Token |
| `PUT` | `/api/orders/{orderId}/status` | Update order status | Service Token |

### Admin Endpoints (ADMIN Role Required)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/api/orders/admin/all` | Get all orders | Admin Role |
| `GET` | `/api/orders/admin/status/{status}` | Get orders by status | Admin Role |

## Key Flows

### 1. User Checkout Flow (Public)
1. **Initiate Checkout**: User calls `POST /api/checkout/initiate?cartId={cartId}`
2. **Validate Cart**: System validates cart ownership and status
3. **Checkout Summary**: User reviews items, totals, and validation
4. **Complete Checkout**: User provides delivery address and completes order
5. **Order Creation**: System creates order, reserves inventory, and marks cart as checked out

### 2. Internal Order Creation
1. **Fetch Cart**: Get cart from CartService (internal)
2. **Validate Items**: Verify inventory via ProductService
3. **Create Order**: Create order and reserve inventory
4. **Update Cart**: Mark cart as checked out
5. **Publish Events**: Send order created event to Kafka

### 3. Order Management
1. **Payment Callbacks**: Handle payment status updates
2. **Inventory Management**: Confirm or release inventory reservations
3. **Status Updates**: Track order through shipping and delivery
4. **Scheduled Tasks**: Auto-cancel old orders and cleanup expired reservations

## Event Integration

### Kafka Topics
- `order.events`: Order lifecycle events

### Event Types
- `ORDER_CREATED`: New order created from cart
- `ORDER_STATUS_UPDATED`: Order status changed
- `ORDER_CANCELLED`: Order cancelled by user or system
- `INVENTORY_RESERVED`: Inventory reserved for order
- `INVENTORY_CONFIRMED`: Inventory usage confirmed

## Security Model

- **Public Endpoints**: Require valid JWT token
- **Internal Endpoints**: Require `X-Service-Token` header
- **Admin Endpoints**: Require `ROLE_ADMIN` role
- **Service Integration**: Secure inter-service communication

## Build & Run

```bash
# Development
./mvnw -DskipTests spring-boot:run

# Production
./mvnw -DskipTests package && java -jar target/OrderManagementService-*.jar
```

## Service Port

- **Default**: `8083`

## Design & Implementation Notes

- **Requirements**: Order lifecycle, inventory sync, payment integration, events
- **Design**: Services for cart/product/payment integrations, events for downstream notification
- **Security**: Controller-level validation of `X-Service-Token` across internal routes
- **Extensibility**: Support for multiple payment methods, shipping providers, and order types

## Monitoring & Health

- **Health Endpoint**: `/actuator/health`
- **Metrics**: Prometheus-compatible metrics
- **Logging**: Structured logging with SLF4J
- **Performance**: Order processing time and inventory reservation tracking

## Checkout Flow Architecture

The service provides a complete user-facing checkout experience:

1. **CheckoutController** - Public endpoints for user checkout flow
2. **CartIntegrationService** - Bridges Cart and Order services
3. **Public Order Endpoints** - User can view and manage their orders

**User Journey:**
1. User builds cart in CartService
2. User initiates checkout via `POST /api/checkout/initiate`
3. User reviews summary and provides delivery address
4. User completes checkout via `POST /api/checkout/complete`
5. Order is created and cart is marked as checked out
6. User can track orders via `/api/orders/my-orders/*` endpoints


