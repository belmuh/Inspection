
# Inspection Management System

A Spring Boot REST API application for managing inspection records with comprehensive CRUD operations and monitoring capabilities.

## Key Features

- RESTful API Design: Clean and intuitive REST endpoints following industry standards
- Spring Boot Architecture: Modern Spring Boot 3.x with best practices
- Health Monitoring: Production-ready health checks and metrics via Spring Actuator
- Database Integration: JPA/Hibernate with flexible database support
- Error Handling: Comprehensive exception handling and validation
- Development Tools: Demo scripts and monitoring utilities included

## Technology Stack

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Database**: H2 (development), PostgreSQL/MySQL (production ready)
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Monitoring**: Spring Boot Actuator

## Prerequisites

- Java 21
- Maven 3.9.10

## Quick Start

### 1. Clone and Build
```bash
git clone <repository-url>
cd inspection-management-system
./mvnw clean install
```

### 2. Run Application
```bash
./mvnw spring-boot:run
```

Application starts at `http://localhost:8080`

### 3. Verify Setup
```bash
curl http://localhost:8080/actuator/health
# Response: {"status":"UP"}
```

## API Endpoints

### System Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Performance metrics
- `GET /actuator/metrics/http.server.requests` - HTTP request metrics

### Test Endpoints
- `GET /api/v1/test/log` - Application health status
- `GET /api/v1/test/exception` - Performance metrics

### Inspection Management (REST API)
- `GET /api/v1/inspections` - Retrieve all inspections
- `GET /api/v1/inspections/{id}` - Get inspection by ID
- `POST /api/v1/inspections` - Create new inspection
- `PUT /api/v1/inspections/{id}` - Update existing inspection
- `DELETE /api/v1/inspections/{id}` - Remove inspection

## API Documentation (Swagger UI)

Explore and test the API endpoints interactively using Swagger UI:

- Open your browser and go to:  
  `http://localhost:8080/swagger-ui.html`

- For raw OpenAPI JSON specification:  
  `http://localhost:8080/v3/api-docs`

## Sample Usage

### API Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Get System Metrics
```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Test Inspection Endpoint
```bash
curl http://localhost:8080/api/v1/inspections
```

## Database Configuration

### Development Environment
```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:inspectiondb
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### Production Configuration
```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/inspection_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Development Tools

### Demo Script
Run the provided demo script to test all endpoints:
```bash
chmod +x scripts/demo.sh
./scripts/demo.sh
```

### Monitor Script
Check system health and metrics:
```bash
chmod +x scripts/monitor.sh
./scripts/monitor.sh
```

## Project Architecture

```
src/main/java/com/company/inspection/
├── InspectionApplication.java          # Main application class
├── controller/                         # REST controllers
├── service/                           # Business logic layer
├── repository/                        # Data access layer
├── model/                            # Entity classes
└── dto/                              # Data transfer objects

src/main/resources/
├── application.properties             # Application configuration
└── data.sql                          # Sample data (if needed)
└── schema.sql                          # schema data (if needed)

scripts/
├── demo.sh                           # API demonstration script
└── monitor.sh                        # System monitoring script
```

## Deployment Options

### Local Development
```bash
./mvnw spring-boot:run
```

### Production JAR
```bash
./mvnw clean package
java -jar target/inspection-management-system-1.0.0.jar
```

### Docker Ready
```bash
# Build image
docker build -t inspection-api .

# Run container
docker run -p 8080:8080 inspection-api
```

## Configuration

### Environment Variables
- `SERVER_PORT`: Application port (default: 8080)
- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

### Application Profiles
- `dev`: Development configuration with H2 database
- `prod`: Production configuration with PostgreSQL/MySQL

## Monitoring & Health Checks

The application includes comprehensive monitoring via Spring Boot Actuator:

- **Health Status**: Real-time application health
- **Metrics**: HTTP requests, memory usage, database connections
- **Performance**: Response times and throughput statistics

## Troubleshooting

### Common Solutions

**Port conflict (8080 in use)**
```bash
lsof -i :8080  # Find process using port
kill -9 <PID>  # Terminate process
```

**Build issues**
```bash
./mvnw clean install -U  # Clean build with dependency update
```

**Database connection problems**
- Verify database service is running
- Check connection credentials in application.properties
- Ensure proper network access

## Technical Highlights

- **Clean Architecture**: Proper separation of concerns with controller-service-repository layers
- **Production Ready**: Health checks, metrics, and proper error handling
- **Flexible Configuration**: Environment-based configuration support
- **Database Agnostic**: Supports multiple database systems via JPA
- **RESTful Design**: Following REST API best practices and conventions
- **Development Efficiency**: Includes utility scripts for testing and monitoring

## Development Status

This project demonstrates:
- Spring Boot framework proficiency
- RESTful API development skills
- Database integration with JPA/Hibernate
- Production-ready application configuration
- Clean code architecture and best practices

---

**Version**: 1.0.0 | **Java**: 21 | **Framework**: Spring Boot 3.5.4
