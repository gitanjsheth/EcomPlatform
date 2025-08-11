# CartService

Robust cart management microservice for the e-commerce platform. Manages user and guest carts, supports inventory-aware operations, Redis-based caching, and MongoDB persistence. Designed to integrate seamlessly with ProductService and OrderManagementService.

## Features

- **Cart Management**: User and guest cart operations with session support
- **Inventory Awareness**: Real-time inventory validation and availability checks
- **Redis Caching**: High-performance cart data caching
- **MongoDB Persistence**: Flexible document-based cart storage
- **Guest Cart Support**: Anonymous shopping with session-based carts
- **Cart Merging**: Seamless guest-to-user cart conversion
- **Event-Driven**: Kafka integration for cart analytics

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 24
- **Database**: MongoDB 6.x (Spring Data MongoDB)
- **Cache**: Redis 7.x (Spring Data Redis)
- **Message Broker**: Apache Kafka 3.x
- **Security**: JWT Authentication
- **Build Tool**: Maven

## Prerequisites

- Java 24+
- MongoDB 6.x
- Redis 7.x
- Apache Kafka 3.x
- Maven 3.6+

## Configuration

### Application Properties

```properties
# Server Configuration
server.port=8082

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/cartservice
spring.data.mongodb.auto-index-creation=true

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=1
spring.data.redis.timeout=60000ms

# Service URLs
app.auth-service.url=http://localhost:8080
app.product-service.url=http://localhost:8081

# Cart Configuration
app.cart.expiry-days=30
app.cart.cache-ttl-hours=1
app.cart.guest-cart-ttl-hours=24
app.cart.max-items-per-cart=100
app.cart.cleanup-interval-hours=6
app.cart.inventory-check-enabled=true

# Security Configuration
app.security.jwt.secret=${JWT_SECRET:defaultSecretForDevelopment}

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

## Setup Instructions

### 1. MongoDB Setup

```bash
# Start MongoDB server
mongod

# Or using Docker
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  mongo:6.0
```

### 2. Redis Setup

```bash
# Start Redis server
redis-server

# Or using Docker
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7.0
```

### 3. Kafka Setup

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 4. Application Startup

```bash
./mvnw -DskipTests spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/CartService-*.jar
```

## API Endpoints

### Public Endpoints (Authenticated End-User)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/api/carts` | Get current cart by user/session | JWT Token |
| `POST` | `/api/carts/add` | Add item to cart | JWT Token |
| `PUT` | `/api/carts/items/{productId}` | Update cart item quantity | JWT Token |
| `DELETE` | `/api/carts/items/{productId}` | Remove item from cart | JWT Token |
| `DELETE` | `/api/carts/clear` | Clear entire cart | JWT Token |
| `POST` | `/api/carts/merge?sessionId=...` | Merge guest cart into user cart | JWT Token |
| `GET` | `/api/carts/count` | Get cart item count | JWT Token |
| `GET` | `/api/carts/contains/{productId}` | Check if product is in cart | JWT Token |

### Internal Endpoints (Service-to-Service)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/api/carts/{cartId}` | Get cart by ID | Service Token |
| `POST` | `/api/carts/{cartId}/checkout` | Mark cart as checked out | Service Token |

## Security Model

- **End-user Cart Endpoints**: Require JWT (set by API gateway/edge)
- **Guest Flows**: Allowed via session ID header
- **Internal Endpoints**: Require `X-Service-Token` header
- **CORS**: Restricted to specific origins

### Headers Used

```
Authorization: Bearer <JWT>
X-Session-ID: <guest-session-id>
X-Service-Token: <internal-service-token>
```

## Event Integration

### Kafka Topics
- `cart.events`: Cart lifecycle events

### Event Types
- `ITEM_ADDED`: Product added to cart
- `ITEM_UPDATED`: Cart item quantity changed
- `ITEM_REMOVED`: Product removed from cart
- `CART_CLEARED`: Entire cart cleared
- `CART_CHECKED_OUT`: Cart marked for checkout

## Build & Run

```bash
# Development
./mvnw -DskipTests spring-boot:run

# Production
./mvnw -DskipTests package && java -jar target/CartService-*.jar
```

## Service Port

- **Default**: `8082`

## Design & Implementation Notes

- **Requirements**: Guest + authenticated carts, merge flow, inventory-aware checks
- **Design**: MongoDB document model with TTL index on `expiresAt`, Redis cache for carts and product availability
- **Security**: JWT for end-user routes, `X-Service-Token` for internal routes, CORS restricted
- **Extensibility**: Support for rate limiting, item-level metadata, and promotions

## Monitoring & Health

- **Health Endpoint**: `/actuator/health`
- **Metrics**: Prometheus-compatible metrics
- **Logging**: Structured logging with SLF4J
- **Performance**: Cart operation timing and cache hit rates

## Interactions Overview (Platform Flow)

1. **User/guest builds a cart** in CartService
2. **OrderManagementService fetches cart** by ID (internal) and creates order
3. **Cart marked as checked out** internally by OrderManagementService
4. **CartService publishes events** on add/update/remove/clear for analytics or personalization


