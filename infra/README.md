# Infrastructure

Infrastructure components and configurations for the E-commerce Platform. Includes API Gateway setup, load balancer configuration, and deployment infrastructure.

## Features

- **API Gateway**: Kong-based API gateway with service routing and security
- **Load Balancing**: AWS ELB configuration for high availability
- **Service Discovery**: Centralized service routing and management
- **Security**: Rate limiting, authentication, and CORS configuration
- **Monitoring**: Health checks and service status monitoring
- **Scalability**: Auto-scaling and load distribution capabilities
- **Deployment**: Infrastructure as Code with Terraform

## Technology Stack

- **API Gateway**: Kong 3.x
- **Load Balancer**: AWS Elastic Load Balancer (ELB)
- **Infrastructure as Code**: Terraform 1.x
- **Container Orchestration**: Docker & Docker Compose
- **Monitoring**: Prometheus & Grafana (planned)
- **Security**: JWT validation, rate limiting, CORS

## Prerequisites

- Docker & Docker Compose
- Terraform 1.x
- AWS CLI configured
- Kong CLI (optional)
- Access to AWS resources

## Configuration

### Application Properties

```properties
# AWS Configuration
aws.region=${AWS_DEFAULT_REGION:us-east-1}
aws.access-key=${AWS_ACCESS_KEY_ID:your-access-key}
aws.secret-key=${AWS_SECRET_ACCESS_KEY:your-secret-key}

# Kong Configuration
kong.database=postgres
kong.pg.host=localhost
kong.pg.port=5432
kong.pg.database=kong
kong.pg.user=kong
kong.pg.password=kong

# Service URLs
app.user-auth-service.url=http://localhost:8080
app.product-service.url=http://localhost:8081
app.cart-service.url=http://localhost:8082
app.order-service.url=http://localhost:8083
app.payment-service.url=http://localhost:8084
app.notification-service.url=http://localhost:8085

# Security Configuration
app.service.token=${INTERNAL_SERVICE_TOKEN:internal-service-secret-2024}
```

## Setup Instructions

### 1. API Gateway Setup

```bash
# Navigate to infra directory
cd infra

# Start Kong with PostgreSQL
docker-compose up -d kong-database
docker-compose up -d kong-migration
docker-compose up -d kong

# Verify Kong is running
curl -i http://localhost:8001/status
```

### 2. Load Balancer Setup

```bash
# Navigate to terraform directory
cd infra/elb/terraform

# Initialize Terraform
terraform init

# Apply configuration
terraform apply
```

### 3. Service Configuration

```bash
# Apply Kong configuration
curl -X POST http://localhost:8001/services \
  -d name=user-auth-service \
  -d url=http://localhost:8080

# Add routes
curl -X POST http://localhost:8001/services/user-auth-service/routes \
  -d paths[]=/auth \
  -d name=auth-route
```

## API Endpoints

### Kong Admin API

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/status` | Kong status and health | None |
| `GET` | `/services` | List all services | None |
| `POST` | `/services` | Create new service | None |
| `GET` | `/services/{service}` | Get service details | None |
| `PUT` | `/services/{service}` | Update service | None |
| `DELETE` | `/services/{service}` | Delete service | None |

### Service Routes

| Service | Route Path | External URL | Internal URL |
|---------|------------|--------------|--------------|
| UserAuthService | `/auth/*`, `/profile/*` | `http://localhost:8000/auth/*` | `http://localhost:8080/*` |
| ProductService | `/products/*`, `/categories/*`, `/search/*` | `http://localhost:8000/products/*` | `http://localhost:8081/*` |
| CartService | `/api/carts/*` | `http://localhost:8000/api/carts/*` | `http://localhost:8082/*` |
| OrderManagementService | `/api/orders/*`, `/api/checkout/*` | `http://localhost:8000/api/orders/*` | `http://localhost:8083/*` |
| PaymentService | `/api/payments/*` | `http://localhost:8000/api/payments/*` | `http://localhost:8084/*` |
| NotificationService | `/api/notifications/*` | `http://localhost:8000/api/notifications/*` | `http://localhost:8085/*` |

## Key Flows

### 1. API Gateway Flow
1. **Request Routing**: External requests hit Kong gateway
2. **Authentication**: JWT validation for protected endpoints
3. **Rate Limiting**: Request throttling per service
4. **Service Forwarding**: Requests forwarded to appropriate microservice
5. **Response Handling**: Responses returned through gateway

### 2. Load Balancer Flow
1. **Traffic Distribution**: AWS ELB distributes incoming traffic
2. **Health Checks**: Regular health monitoring of service instances
3. **Auto-scaling**: Automatic scaling based on load
4. **Failover**: Automatic failover to healthy instances

### 3. Service Discovery
1. **Service Registration**: Kong maintains service endpoints
2. **Health Monitoring**: Continuous health status checking
3. **Load Balancing**: Distribution across multiple instances
4. **Failover**: Automatic routing to healthy services

## Event Integration

### Infrastructure Events
- **Service Health**: Health status changes
- **Load Balancer**: Traffic pattern changes
- **Gateway Metrics**: Request/response statistics

### Monitoring Integration
- **Health Endpoints**: `/actuator/health` across all services
- **Metrics Collection**: Prometheus-compatible metrics
- **Logging**: Centralized logging infrastructure

## Security Model

- **Public Endpoints**: Health checks and status endpoints
- **Protected Endpoints**: All service endpoints require authentication
- **Gateway Security**: JWT validation, rate limiting, CORS
- **Infrastructure Security**: AWS IAM roles and security groups

## Build & Run

```bash
# Start infrastructure services
docker-compose up -d

# Deploy load balancer
cd infra/elb/terraform
terraform apply

# Verify deployment
curl -i http://localhost:8001/status
```

## Service Port

- **Kong Admin**: `8001`
- **Kong Proxy**: `8000`
- **PostgreSQL**: `5432`

## Design & Implementation Notes

- **Requirements**: Centralized routing, load balancing, service discovery
- **Design**: Kong API gateway with AWS ELB for scalability
- **Security**: JWT validation, rate limiting, and CORS configuration
- **Extensibility**: Support for additional services and advanced monitoring

## Monitoring & Health

- **Health Endpoint**: `/status` (Kong), `/actuator/health` (Services)
- **Metrics**: Request/response metrics and latency tracking
- **Logging**: Structured logging with centralized collection
- **Performance**: Gateway performance and service health monitoring
