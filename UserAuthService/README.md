# UserAuthService

Authentication and user management microservice for the e-commerce platform. Issues and validates JWTs, manages users/roles, supports token blacklist via Redis, and exposes token validation for other services.

## Features

- **User Authentication**: Secure signup, login, and logout functionality
- **JWT Management**: Token generation, validation, and blacklisting
- **User Profile Management**: View and update user profile information
- **Password Reset**: Secure password reset with email notifications
- **Role-Based Access Control**: Admin and user role management
- **Token Security**: Redis-based token blacklist with TTL
- **Event-Driven**: Kafka integration for user events

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 24
- **Database**: MySQL 8.0+ (Spring Data JPA, Flyway)
- **Cache**: Redis 7.x (Spring Data Redis)
- **Security**: Spring Security, JWT (JJWT 0.12.x)
- **Message Broker**: Apache Kafka 3.x
- **Build Tool**: Maven

## Prerequisites

- Java 24+
- MySQL 8.0+
- Redis 7.x
- Apache Kafka 3.x
- Maven 3.6+

## Configuration

### Application Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/userauthproj
spring.datasource.username=gitanjsheth
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration
app.jwt.secret=${JWT_SECRET:lLuFR8mYOyR74tJmzP40E13/YpBVhfH3jr+g5EHxGfc=}
app.jwt.expiration-days=30
app.jwt.admin-expiration-days=7
app.jwt.remember-me-expiration-days=90

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
spring.data.redis.timeout=60000ms

# Security Configuration
app.security.max-login-attempts=5
app.security.account-lock-duration-minutes=15

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

## Setup Instructions

### 1. Database Setup

```sql
CREATE DATABASE userauthproj;
CREATE USER 'gitanjsheth'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON userauthproj.* TO 'gitanjsheth'@'localhost';
FLUSH PRIVILEGES;
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
./mvnw -DskipTests package && java -jar target/UserAuthService-*.jar
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/auth/signup` | Create new user account | Public |
| `POST` | `/auth/login` | User login | Public |
| `POST` | `/auth/validate-token` | Validate JWT token | Bearer Token |
| `POST` | `/auth/logout` | User logout | Bearer Token |
| `POST` | `/auth/admin/users/{userId}/ban` | Ban user (Admin only) | Admin Role |

### User Profile Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/profile` | Get user profile | Authenticated |
| `PUT` | `/profile` | Update user profile | Authenticated |

### Password Reset

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/auth/password/request` | Request password reset | Public |
| `POST` | `/auth/password/reset` | Reset password with token | Public |

## Security Model

- **Public Endpoints**: `/auth/signup`, `/auth/login`, `/auth/password/*`
- **Protected Endpoints**: `/profile/*` (requires valid JWT)
- **Admin Endpoints**: User ban functionality (requires `ROLE_ADMIN`)
- **Token Validation**: Other services can validate tokens via `/auth/validate-token`
- **Stateless Design**: No server-side sessions, JWT-based authentication

## Event Integration

### Kafka Topics
- `user.events`: User lifecycle events

### Event Types
- `USER_REGISTERED`: New user account created
- `PASSWORD_RESET_REQUESTED`: Password reset initiated
- `USER_BANNED`: User account banned by admin

## Build & Run

```bash
# Development
./mvnw -DskipTests spring-boot:run

# Production
./mvnw -DskipTests package && java -jar target/UserAuthService-*.jar
```

## Service Port

- **Default**: `8080`

## Design & Implementation Notes

- **Requirements**: Secure authentication, token management, user profile management
- **Design**: JPA models for users/roles, JWT contains userId/username/roles, Redis blacklist with JTI TTL
- **Security**: Stateless design, other services call `/auth/validate-token` for verification
- **Extensibility**: Support for social media login, multi-factor authentication, and advanced security features

## Monitoring & Health

- **Health Endpoint**: `/actuator/health`
- **Metrics**: Prometheus-compatible metrics
- **Logging**: Structured logging with SLF4J
- **Security**: Login attempt tracking and account lockout protection


