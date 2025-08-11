# ProductService

A microservice responsible for managing the product catalog, search functionality, and inventory management for the Ecommerce Platform.

## Features

- **Product Management**: CRUD operations for products with categories
- **Advanced Search**: Full-text search powered by Elasticsearch
- **Inventory Management**: Stock tracking with reservation system
- **Category Management**: Hierarchical product categorization
- **Search Analytics**: Performance tracking and search insights
- **Event-Driven**: Kafka integration for inventory events

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: MySQL 8.0+ (Spring Data JPA, Flyway)
- **Search Engine**: Elasticsearch 8.x
- **Message Broker**: Apache Kafka 3.x
- **Security**: JWT Authentication
- **Build Tool**: Maven

## Prerequisites

- Java 17+
- MySQL 8.0+
- Elasticsearch 8.x
- Apache Kafka 3.x
- Maven 3.6+

## Configuration

### Application Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/productserviceproj
spring.datasource.username=gitanjsheth
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.connection-timeout=5000
spring.elasticsearch.socket-timeout=60000
spring.elasticsearch.index-name=products
spring.elasticsearch.number-of-shards=1
spring.elasticsearch.number-of-replicas=0

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=product-service
spring.kafka.consumer.auto-offset-reset=earliest

# Authentication Service
app.auth-service.url=http://localhost:8080
```

## Setup Instructions

### 1. Database Setup

```sql
CREATE DATABASE productserviceproj;
CREATE USER 'gitanjsheth'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON productserviceproj.* TO 'gitanjsheth'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Elasticsearch Setup

#### Local Development
```bash
# Download and start Elasticsearch
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.11.0-linux-x86_64.tar.gz
tar -xzf elasticsearch-8.11.0-linux-x86_64.tar.gz
cd elasticsearch-8.11.0
./bin/elasticsearch
```

#### Docker
```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
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
./mvnw -DskipTests package && java -jar target/ProductService-*.jar
```

## API Endpoints

### Product Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/products` | Get all products | Public |
| `GET` | `/products/{id}` | Get product by ID | Public |
| `POST` | `/products` | Create new product | Admin |
| `PUT` | `/products/{id}` | Update product | Admin |
| `DELETE` | `/products/{id}` | Delete product | Admin |
| `PATCH` | `/products/{id}` | Soft delete product | Admin |

### Category Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/categories` | Get all categories | Public |
| `GET` | `/categories/{id}` | Get category by ID | Public |
| `POST` | `/categories` | Create new category | Admin |
| `PUT` | `/categories/{id}` | Update category | Admin |
| `DELETE` | `/categories/{id}` | Delete category | Admin |

### Search Functionality

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/search?q={query}` | Search products | Public |
| `GET` | `/search/autocomplete?q={prefix}` | Get search suggestions | Public |

### Product Browsing

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/products/category/{categoryId}` | Get products by category ID | Public |
| `GET` | `/products/category/title/{categoryTitle}` | Get products by category title | Public |

## Search Features

### Full-Text Search
- **Multi-field search**: Searches across product title and description
- **Fuzzy matching**: Handles typos and spelling variations
- **Relevance scoring**: Results ranked by relevance
- **Pagination**: Configurable page size and navigation

### Autocomplete
- **Prefix matching**: Real-time suggestions as you type
- **Performance optimized**: Fast response times for better UX
- **Configurable results**: Adjustable suggestion count

### Search Analytics
- **Performance tracking**: Query execution time monitoring
- **Usage analytics**: Search patterns and popular queries
- **User behavior**: User agent and IP tracking for insights

## Event Integration

### Kafka Topics
- `inventory.events`: Inventory updates and stock changes
- `product.events`: Product lifecycle events

### Event Types
- `PRODUCT_CREATED`: New product added
- `PRODUCT_UPDATED`: Product information modified
- `INVENTORY_UPDATED`: Stock quantity changes
- `PRODUCT_DELETED`: Product removal

## Security

- **JWT Authentication**: Secure API access
- **Role-based Access**: Admin and User permissions
- **CORS Configuration**: Cross-origin request handling
- **Input Validation**: Comprehensive data validation

## Build & Run

```bash
# Development
./mvnw -DskipTests spring-boot:run

# Production
./mvnw -DskipTests package && java -jar target/ProductService-*.jar
```

## Service Port

- **Default**: `8081`

## Design & Implementation Notes

- **Requirements**: Product catalog management, advanced search, inventory tracking
- **Design**: MySQL for structured data, Elasticsearch for search, Kafka for events
- **Security**: JWT-based authentication with role-based access control
- **Extensibility**: Support for product variants, pricing tiers, and advanced inventory management

## Monitoring & Health

- **Health Endpoint**: `/actuator/health`
- **Metrics**: Prometheus-compatible metrics
- **Logging**: Structured logging with SLF4J
- **Performance**: Query execution time tracking and search analytics