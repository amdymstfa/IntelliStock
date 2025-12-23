# IntelliStock - AI-Powered Multi-Warehouse Stock Management System 📦🤖

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Testing](#testing)
- [Deployment](#deployment)
- [Team](#team)

---

## 🎯 Overview

IntelliStock is an intelligent inventory management system designed for multi-warehouse operations. It combines traditional stock management with AI-powered predictions to optimize inventory levels, reduce costs, and prevent stockouts.

### Key Problems Solved

1. **Stock Prediction**: Prevents stockouts and overstocking using AI analysis
2. **Security & Access Control**: Role-based access with data encryption
3. **Multi-Warehouse Management**: Centralized control with distributed operations
4. **Sales Analytics**: Historical data analysis for informed decision-making

---

## ✨ Features

### 🤖 AI-Powered Predictions
- 30-day demand forecasting
- Trend analysis and seasonality detection
- Automated reorder recommendations
- Confidence level indicators

### 🔐 Advanced Security
- JWT-based stateless authentication
- Role-based access control (ADMIN / MANAGER)
- AES encryption for sensitive data (purchase prices, margins)
- Warehouse-level data isolation

### 📊 Stock Management
- Real-time inventory tracking
- Low stock alerts
- Multi-warehouse stock distribution
- Product categorization

### 📈 Sales Analytics
- Historical sales tracking
- Day-of-week patterns
- Monthly/yearly trends
- Warehouse-specific insights

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.5.9** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **Liquibase** - Database version control

### Database
- **PostgreSQL 15** - Primary database (Production/Dev)
- **H2** - In-memory database (Testing)

### Security
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **BCrypt** - Password hashing
- **AES** - Data encryption

### AI/ML
- **Apache Commons Math 3** - Statistical analysis & predictions

### Documentation
- **SpringDoc OpenAPI 3** - API documentation
- **Swagger UI** - Interactive API explorer

### DevOps
- **Docker & Docker Compose** - Containerization
- **GitHub Actions** - CI/CD pipeline
- **Maven** - Build automation

### Testing
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing

---

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         Controllers (REST API)          │
├─────────────────────────────────────────┤
│              Services                   │
├─────────────────────────────────────────┤
│            Repositories                 │
├─────────────────────────────────────────┤
│        Entities (JPA Models)            │
├─────────────────────────────────────────┤
│           Database (PostgreSQL)         │
└─────────────────────────────────────────┘
```

### Project Structure

```
src/
├── main/
│   ├── java/com/logistics/intellistock/
│   │   ├── config/              # Configuration classes
│   │   │   ├── SecurityConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   ├── DataInitializer.java
│   │   │   └── ...
│   │   ├── controller/          # REST controllers
│   │   │   ├── AuthController.java
│   │   │   ├── ProductController.java
│   │   │   ├── StockController.java
│   │   │   └── ...
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── entity/              # JPA entities
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Stock.java
│   │   │   └── ...
│   │   ├── repository/          # Data repositories
│   │   ├── service/             # Business logic
│   │   │   └── impl/
│   │   ├── security/            # Security components
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── SecurityUtils.java
│   │   │   └── ...
│   │   ├── exception/           # Exception handling
│   │   ├── validation/          # Custom validators
│   │   ├── mapper/              # DTO mappers
│   │   ├── enums/               # Enumerations
│   │   ├── ai/                  # AI prediction engine
│   │   └── util/                # Utility classes
│   └── resources/
│       ├── application.yaml     # Main configuration
│       ├── application-dev.yaml
│       ├── application-prod.yaml
│       └── db/
│           └── changelog/       # Liquibase migrations
└── test/                        # Test classes
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** installed
- **Maven 3.8+** installed
- **Docker & Docker Compose** (recommended) OR **PostgreSQL 15+**
- **Git** for version control

### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/intellistock.git
cd intellistock
```

#### 2. Configure Environment

Create a `.env` file in the project root:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/intellistock_db
DATABASE_USERNAME=intellistock_user
DATABASE_PASSWORD=intellistock_password

# JWT
JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits

# Encryption
ENCRYPTION_KEY=your-encryption-key-32-characters
```

#### 3. Start PostgreSQL (Option A: Docker)

```bash
docker-compose up -d postgres
```

#### 3. Start PostgreSQL (Option B: Local Installation)

```bash
# Create database and user
sudo -u postgres psql

CREATE DATABASE intellistock_db;
CREATE USER intellistock_user WITH PASSWORD 'intellistock_password';
GRANT ALL PRIVILEGES ON DATABASE intellistock_db TO intellistock_user;
\q
```

#### 4. Build the Application

```bash
mvn clean install
```

#### 5. Run the Application

```bash
# Development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The application will start on **http://localhost:8080**

### Verify Installation

```bash
# Check health
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP"}
```

---

## 📖 API Documentation

### Accessing API Docs

Once the application is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Authentication

All endpoints (except `/api/v1/auth/**`) require authentication.

#### 1. Login

```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "login": "admin",
  "password": "Admin@123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "login": "admin",
  "email": "admin@intellistock.com",
  "role": "ADMIN",
  "warehouseId": null,
  "warehouseName": null
}
```

#### 2. Use Token in Requests

```bash
GET /api/v1/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Default Users

| Username | Password | Role | Warehouse |
|----------|----------|------|-----------|
| admin | Admin@123 | ADMIN | All |
| manager.ny | Manager@123 | MANAGER | New York |
| manager.la | Manager@123 | MANAGER | Los Angeles |
| manager.chicago | Manager@123 | MANAGER | Chicago |
| manager.miami | Manager@123 | MANAGER | Miami |

### Key Endpoints

#### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - Register new user (ADMIN only)

#### Products
- `GET /api/v1/products` - List all products
- `GET /api/v1/products/{id}` - Get product details
- `POST /api/v1/products` - Create product (ADMIN only)
- `PUT /api/v1/products/{id}` - Update product (ADMIN only)
- `DELETE /api/v1/products/{id}` - Delete product (ADMIN only)

#### Stocks
- `GET /api/v1/stocks/warehouse/{warehouseId}` - Get warehouse stocks
- `GET /api/v1/stocks/low` - Get low stock items
- `PUT /api/v1/stocks/{productId}/{warehouseId}` - Update stock

#### Sales History
- `POST /api/v1/sales` - Record sale
- `GET /api/v1/sales/warehouse/{warehouseId}` - Get sales history

#### Predictions
- `GET /api/v1/predictions/warehouse/{warehouseId}` - Get predictions
- `POST /api/v1/predictions/generate/{productId}/{warehouseId}` - Generate prediction

---

## 🔐 Security

### Authentication Flow

1. User sends credentials to `/api/v1/auth/login`
2. System validates credentials
3. JWT token generated and returned
4. Client includes token in `Authorization` header
5. JWT filter validates token on each request
6. User context established for authorization

### Role-Based Access Control

| Feature | ADMIN | MANAGER |
|---------|-------|---------|
| View all warehouses | ✅ | ❌ (own only) |
| View purchase prices/margins | ✅ | ❌ |
| Manage products | ✅ | ❌ |
| Manage warehouses | ✅ | ❌ |
| Manage users | ✅ | ❌ |
| Update stocks | ✅ | ✅ (own warehouse) |
| View sales history | ✅ | ✅ (own warehouse) |
| View predictions | ✅ | ✅ (filtered) |

### Data Encryption

- **Purchase Prices**: Encrypted with AES before storage
- **Margins**: Encrypted with AES before storage
- **Passwords**: Hashed with BCrypt (strength 10)

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ProductServiceTest
```

### Run Integration Tests

```bash
mvn verify -P integration-tests
```

### Test Coverage Report

```bash
mvn jacoco:report
```

View report at: `target/site/jacoco/index.html`

### Test Structure

```
src/test/java/
├── controller/          # Controller tests
├── service/             # Service tests
├── repository/          # Repository tests
├── security/            # Security tests
├── ai/                  # AI engine tests
└── integration/         # Integration tests
```

---

## 🐳 Deployment

### Docker Deployment

#### Build Docker Image

```bash
docker build -t intellistock:latest .
```

#### Run with Docker Compose

```bash
docker-compose up -d
```

This starts:
- PostgreSQL database
- IntelliStock application
- pgAdmin (optional)

#### Access Services

- **Application**: http://localhost:8080
- **pgAdmin**: http://localhost:5050 (admin@intellistock.com / admin)

### Production Deployment

#### 1. Update Environment Variables

```bash
export DATABASE_URL=jdbc:postgresql://prod-db:5432/intellistock_db
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=your_secure_password
export JWT_SECRET=your_production_jwt_secret
export ENCRYPTION_KEY=your_production_encryption_key
```

#### 2. Build Production JAR

```bash
mvn clean package -Pprod -DskipTests
```

#### 3. Run Application

```bash
java -jar target/intellistock-1.0.0.jar --spring.profiles.active=prod
```

### CI/CD Pipeline

The project includes a GitHub Actions workflow:

```
.github/workflows/ci-cd.yml
```

**Pipeline Steps:**
1. Build and compile
2. Run unit tests
3. Run integration tests
4. Generate coverage report
5. Build Docker image
6. Push to Docker registry (on main branch)

---

## 📊 Database Schema

### Main Tables

- **users** - User accounts and roles
- **warehouses** - Warehouse locations
- **products** - Product catalog
- **stocks** - Inventory levels
- **sales_history** - Sales records
- **predictions** - AI predictions

### ER Diagram

```
users ──┐
        │
        ├── warehouse (FK)
        │
warehouses ──┬── stocks ──── products
             │
             ├── sales_history ──── products
             │
             └── predictions ──── products
```

---

## 🤖 AI Prediction Engine

### How It Works

1. **Data Collection**: Gathers last 90 days of sales data
2. **Trend Analysis**: Calculates moving averages and trends
3. **Seasonality Detection**: Identifies weekly/monthly patterns
4. **Prediction Generation**: Forecasts next 30 days demand
5. **Recommendation**: Suggests reorder quantities

### Algorithm Features

- Moving average calculation
- Standard deviation analysis
- Weekend/weekday pattern detection
- Category-based adjustment
- Confidence level calculation

### Example Recommendations

- "Stock sufficient for next 30 days"
- "Order 150 units - High demand detected"
- "Warning: Unusually low sales - Review marketing"

---

## 🔧 Configuration Profiles

### Development (`dev`)
- H2 console enabled
- SQL logging enabled
- Detailed error messages
- Auto data initialization

### Production (`prod`)
- SQL logging disabled
- Minimal error details
- Environment-based configuration
- No auto data initialization

### Test (`test`)
- In-memory H2 database
- Fast execution
- Isolated test data

---

## 📝 Contributing

### Branch Strategy

- `main` - Production-ready code
- `develop` - Development branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches

### Commit Convention

```
feat: Add new feature
fix: Bug fix
docs: Documentation update
refactor: Code refactoring
test: Add tests
chore: Maintenance tasks
```

---

## 👥 Team

- **Team Member 1** - Backend & Security
- **Team Member 2** - AI & Algorithms
- **Team Member 3** - DevOps & Testing
