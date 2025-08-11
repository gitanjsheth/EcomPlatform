# Ecommerce Platform

A modern, scalable ecommerce platform built with microservices architecture, featuring advanced search capabilities, real-time inventory management, and secure payment processing.

## 🏗️ Architecture Overview

The platform follows a microservices architecture pattern with the following components:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │    │  API Gateway    │    │   Frontend      │
│      (ELB)      │───▶│     (Kong)      │───▶│   (React/Vue)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
        ┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
        │ User Auth    │ │  Product    │ │   Cart     │
        │   Service    │ │  Service    │ │  Service   │
        │   (MySQL)    │ │(MySQL+ES)   │ │(MongoDB)   │
        └──────────────┘ └─────────────┘ └────────────┘
                │               │               │
        ┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
        │   Order      │ │  Payment    │ │Notification│
        │ Management   │ │  Service    │ │  Service   │
        │   (MySQL)    │ │   (MySQL)   │ │   (MySQL)  │
        └──────────────┘ └─────────────┘ └────────────┘
```

## 🚀 Services

### 1. User Authentication Service
- **Port**: 8080
- **Database**: MySQL + Redis
- **Features**: User registration, login, JWT authentication, role management
- **Tech Stack**: Spring Boot, Spring Security, JWT, Redis

### 2. Product Service
- **Port**: 8081
- **Database**: MySQL + Elasticsearch
- **Features**: Product catalog, search, inventory management, categories
- **Tech Stack**: Spring Boot, Spring Data Elasticsearch, Kafka

### 3. Cart Service
- **Port**: 8082
- **Database**: MongoDB + Redis
- **Features**: Shopping cart management, guest cart support
- **Tech Stack**: Spring Boot, Spring Data MongoDB, Redis, Kafka

### 4. Order Management Service
- **Port**: 8083
- **Database**: MySQL
- **Features**: Order processing, checkout, tracking
- **Tech Stack**: Spring Boot, Spring Data JPA, Kafka

### 5. Payment Service
- **Port**: 8084
- **Database**: MySQL
- **Features**: Payment processing, receipts, refunds
- **Tech Stack**: Spring Boot, Stripe integration, Kafka

### 6. Notification Service
- **Port**: 8085
- **Database**: MySQL
- **Features**: Email notifications, templates, event-driven
- **Tech Stack**: Spring Boot, AWS SES, Kafka

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Build Tool**: Maven
- **Security**: JWT, Spring Security

### Data Layer
- **Primary Database**: MySQL 8.0+
- **Search Engine**: Elasticsearch 8.x
- **Document Store**: MongoDB 6.x
- **Cache**: Redis 7.x
- **Message Broker**: Apache Kafka 3.x

### Infrastructure
- **API Gateway**: Kong
- **Load Balancer**: AWS ELB
- **Containerization**: Docker
- **Orchestration**: Docker Compose (local)

## 📋 Prerequisites

- Java 17+
- MySQL 8.0+
- Elasticsearch 8.x
- MongoDB 6.x
- Redis 7.x
- Apache Kafka 3.x
- Maven 3.6+
- Docker & Docker Compose

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd EcomPlatform
```

### 2. Start Infrastructure Services
```bash
# Start MySQL
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=ecomplatform \
  mysql:8.0

# Start Elasticsearch
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Start MongoDB
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  mongo:6.0

# Start Redis
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7.0

# Start Kafka
docker run -d \
  --name zookeeper \
  -p 2181:2181 \
  confluentinc/cp-zookeeper:7.4.0

docker run -d \
  --name kafka \
  -p 9092:9092 \
  --link zookeeper:zookeeper \
  confluentinc/cp-kafka:7.4.0
```

### 3. Configure Databases
```sql
-- Create databases for each service
CREATE DATABASE userauthproj;
CREATE DATABASE productserviceproj;
CREATE DATABASE ordermanagementproj;
CREATE DATABASE paymentserviceproj;
CREATE DATABASE notificationserviceproj;
```

### 4. Start Services
```bash
# Start each service in separate terminals
cd UserAuthService && mvn spring-boot:run
cd ProductService && mvn spring-boot:run
cd CartService && mvn spring-boot:run
cd OrderManagementService && mvn spring-boot:run
cd PaymentService && mvn spring-boot:run
cd NotificationService && mvn spring-boot:run
```

### 5. Start API Gateway
```bash
cd infra/api-gateway
docker-compose up -d
```

## ⚙️ Configuration

### Elasticsearch Configuration

The Product Service includes a dedicated `ElasticsearchConfig` class with the following features:

#### Key Properties
```properties
# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.connection-timeout=5000
spring.elasticsearch.socket-timeout=60000
spring.elasticsearch.index-name=products
spring.elasticsearch.number-of-shards=1
spring.elasticsearch.number-of-replicas=0
# spring.elasticsearch.username=elastic
# spring.elasticsearch.password=changeme
```

#### Configuration Features
- **Connection Management**: Configurable timeouts and connection settings
- **Authentication Support**: Basic auth for secured Elasticsearch clusters
- **Index Configuration**: Customizable shard and replica settings
- **Client Optimization**: Optimized REST client configuration
- **Repository Enablement**: Automatic repository configuration

### Service Configuration

Each service has its own `application.properties` file with service-specific configurations:

- **Database connections**
- **Kafka settings**
- **Service tokens**
- **External service URLs**

## 🔐 Security

### Authentication
- **JWT-based authentication** across all services
- **Role-based access control** (ADMIN, USER)
- **Service-to-service authentication** with service tokens

### API Security
- **CORS configuration** for cross-origin requests
- **Rate limiting** via Kong API Gateway
- **Input validation** and sanitization
- **SQL injection protection**

## 📊 Monitoring & Health

### Health Checks
- **Actuator endpoints** for each service
- **Health indicators** for databases and external services
- **Custom health checks** for business logic

### Metrics
- **Prometheus-compatible metrics**
- **Custom business metrics**
- **Performance monitoring**

### Logging
- **Structured logging** with SLF4J
- **Centralized log aggregation** ready
- **Audit trails** for security events

## 🔄 Event-Driven Architecture

### Kafka Topics
- `user.events`: User lifecycle events
- `product.events`: Product changes
- `cart.events`: Cart operations
- `order.events`: Order lifecycle
- `payment.events`: Payment processing
- `inventory.events`: Stock updates

### Event Types
- **Domain Events**: Business state changes
- **Integration Events**: Service communication
- **Audit Events**: Security and compliance

## 🧪 Testing

### Test Types
- **Unit Tests**: Individual component testing
- **Integration Tests**: Service integration testing
- **End-to-End Tests**: Complete workflow testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific service tests
cd ProductService && mvn test
```

## 🚀 Deployment

### Local Development
```bash
# Start all services with Docker Compose
docker-compose up -d

# Start services individually
mvn spring-boot:run
```

### Production Deployment
- **Container orchestration** with Kubernetes
- **Service mesh** for inter-service communication
- **Load balancing** and auto-scaling
- **Monitoring and alerting**

## 📚 API Documentation

### Service APIs
Each service exposes REST APIs with comprehensive documentation:

- **User Auth**: `/auth/**` endpoints
- **Products**: `/products/**` and `/search/**` endpoints
- **Cart**: `/api/carts/**` endpoints
- **Orders**: `/api/orders/**` and `/api/checkout/**` endpoints
- **Payments**: `/api/payments/**` endpoints
- **Notifications**: `/api/notifications/**` endpoints

### API Gateway
Kong provides centralized API management with:
- **Request routing**
- **Rate limiting**
- **Authentication**
- **CORS handling**

## 🔧 Troubleshooting

### Common Issues

1. **Service Connection Failures**
   - Verify infrastructure services are running
   - Check network connectivity
   - Validate configuration properties

2. **Database Issues**
   - Ensure databases are accessible
   - Check credentials and permissions
   - Verify schema initialization

3. **Elasticsearch Issues**
   - Confirm Elasticsearch is running
   - Check index mappings
   - Verify search queries

### Debug Mode
Enable debug logging in `application.properties`:
```properties
logging.level.com.gitanjsheth=DEBUG
logging.level.org.springframework=DEBUG
```