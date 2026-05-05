# Employee Service

A Spring Boot RESTful API service for employee management with OAuth2 authentication, providing CRUD operations for employee data.

## Overview

The Employee Service is a microservice that handles all employee-related operations including creation, retrieval, updating, and deletion of employee records. It integrates with Keycloak for secure authentication and authorization using JWT tokens.

## Technology Stack

- **Spring Boot 2.7.5** - Application framework
- **Spring Security** - Authentication and authorization
- **OAuth2 Resource Server** - JWT token validation
- **Spring Data JPA** - Database abstraction layer
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **SpringDoc OpenAPI 1.6.12** - API documentation with Swagger UI
- **Java 17** - Runtime environment
- **Maven** - Build and dependency management

## Features

- **Employee CRUD Operations**: Create, read, update, and delete employee records
- **OAuth2 Authentication**: Secure API endpoints with JWT token validation
- **Pagination**: Efficient handling of large employee datasets
- **Data Validation**: Input validation using Bean Validation annotations
- **API Documentation**: Auto-generated OpenAPI/Swagger documentation
- **Error Handling**: Comprehensive exception handling with proper HTTP status codes
- **Date Validation**: Business logic validation for employment dates

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.3+
- Keycloak authorization server running (see `../iam-service/README.md`)

### Running the Service

1. **Build the application**
   ```bash
   mvn clean install
   ```

2. **Start the service**
   ```bash
   mvn spring-boot:run
   ```

3. **Verify the service is running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Access Points

- **Application URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## API Documentation

### Authentication

All API endpoints require OAuth2 authentication. Include a valid JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Endpoints

#### Employee Operations

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| GET | `/employees/{page}/{size}` | Get paginated list of employees | Required |
| GET | `/employees/{id}` | Get employee by ID | Required |
| POST | `/employees` | Create new employee | Required |
| PUT | `/employees/{id}` | Update existing employee | Required |
| DELETE | `/employees/{id}` | Delete employee by ID | Required |

#### Request/Response Examples

##### Create Employee
```json
POST /employees
Content-Type: application/json
Authorization: Bearer <token>

{
  "firstName": "John",
  "middleInitial": "A",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "dateOfEmployment": "2020-06-15"
}
```

##### Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "John",
  "middleInitial": "A",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "dateOfEmployment": "2020-06-15",
  "status": "ACTIVE"
}
```

## Data Model

### Employee Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| id | String | Unique identifier (UUID) | Auto-generated |
| firstName | String | Employee's first name | Required, not blank |
| middleInitial | String | Middle initial | Required, not blank |
| lastName | String | Employee's last name | Required, not blank |
| dateOfBirth | LocalDate | Date of birth | Required, not blank |
| dateOfEmployment | LocalDate | Employment start date | Required, not blank |
| status | EmployeeStatus | Employment status | ACTIVE or INACTIVE |

### Employee Status Enum

- **ACTIVE**: Currently employed
- **INACTIVE**: No longer employed

## Project Structure

```
employee-service/
├── src/main/java/com/employee/
│   ├── controllers/           # REST API endpoints
│   │   └── EmployeeController.java
│   ├── dto/                  # Data Transfer Objects
│   │   ├── EmployeeDto.java
│   │   └── EmployeeRequest.java
│   ├── entities/             # JPA entities
│   │   └── Employee.java
│   ├── enums/                # Enumeration classes
│   │   └── EmployeeStatus.java
│   ├── exceptions/           # Custom exceptions
│   │   ├── EmployeeNotFound.java
│   │   └── InvalidDataException.java
│   ├── exceptions/handlers/  # Exception handlers
│   │   └── GeneralExceptionHandler.java
│   ├── repositories/         # JPA repositories
│   │   └── EmployeeRepository.java
│   ├── services/             # Business logic
│   │   └── EmployeeService.java
│   ├── EmployeeCrudApplication.java  # Main application class
│   ├── SecurityConfiguration.java     # Security configuration
│   └── OpenAPIConfiguration.java     # API documentation config
├── src/main/resources/
│   ├── application.yml       # Application configuration
│   └── EmployeeCrud.postman_collection.json
├── pom.xml                   # Maven configuration
└── README.md                 # This file
```

## Configuration

### Application Properties

Key configuration options in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

security:
  oauth2:
    resourceserver:
      jwt:
        issuer-uri: http://localhost:8083/auth/realms/baeldung
```

### Security Configuration

The service is configured as an OAuth2 Resource Server:

- **JWT Validation**: Tokens are validated against the Keycloak issuer
- **Protected Endpoints**: All `/employees/**` endpoints require authentication
- **CSRF**: Disabled for stateless REST API
- **CORS**: Configured to allow requests from authorized origins

## Development Guide

### Database Access

Access the H2 console for debugging:
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (empty)

### Testing with Postman

1. Import the provided Postman collection:
   - File: `EmployeeCrud.postman_collection.json`

2. Set up OAuth2 authentication:
   - Go to Authorization tab
   - Select "OAuth 2.0" type
   - Configure token endpoint: `http://localhost:8083/auth/realms/baeldung/protocol/openid-connect/token`
   - Add client credentials and user credentials
   - Get access token

### Business Logic Validation

The service includes date validation logic:

- **Date of Birth**: Must be a valid past date
- **Date of Employment**: Must be after date of birth
- **Date Format**: Expected format is `yyyy-MM-dd`

## Error Handling

### HTTP Status Codes

| Status | Description | Example |
|--------|-------------|---------|
| 200 | Success | Employee retrieved/created/updated/deleted |
| 400 | Bad Request | Invalid input data |
| 404 | Not Found | Employee not found |
| 401 | Unauthorized | Invalid or missing JWT token |
| 403 | Forbidden | Insufficient permissions |
| 500 | Internal Server Error | Unexpected server error |

### Error Response Format

```json
{
  "timestamp": "2023-01-01T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "First Name cannot be empty",
  "path": "/employees"
}
```

## Integration

### With IAM Service

The employee service integrates with the IAM service for authentication:

1. **Token Issuance**: IAM service issues JWT tokens
2. **Token Validation**: Employee service validates tokens
3. **User Information**: Extract user details from token claims
4. **Authorization**: Enforce access controls based on user roles

### Database Schema

The employee table schema:

```sql
CREATE TABLE employee (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    middle_initial VARCHAR(10) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    date_of_employment DATE NOT NULL,
    status VARCHAR(15) NOT NULL
);
```

## Development Commands

### Build and Test
```bash
# Clean and build
mvn clean install

# Run tests
mvn test

# Skip tests during build
mvn clean install -DskipTests
```

### Run Application
```bash
# Using Maven
mvn spring-boot:run

# Using Java (after build)
java -jar target/EmployeeService.jar

# With custom port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Dependencies Management
```bash
# Check for updates
mvn versions:display-dependency-updates

# Update versions
mvn versions:use-latest-releases
```

## Production Considerations

For production deployment, consider:

1. **External Database**: Replace H2 with PostgreSQL, MySQL, or other production database
2. **Connection Pooling**: Configure appropriate connection pool settings
3. **Logging**: Configure structured logging with appropriate levels
4. **Monitoring**: Add metrics and health checks
5. **Security**: Enable HTTPS and secure configuration
6. **Performance**: Implement caching where appropriate
7. **Backup Strategy**: Regular database backups
8. **Load Balancing**: Configure for horizontal scaling

## API Best Practices

- **Use appropriate HTTP methods**: GET for retrieval, POST for creation, PUT for updates, DELETE for deletion
- **Implement proper error handling**: Return meaningful error messages and HTTP status codes
- **Validate input data**: Use Bean Validation annotations
- **Use pagination**: For large datasets, implement pagination to improve performance
- **Version your API**: Consider API versioning for backward compatibility
- **Document your API**: Maintain comprehensive API documentation

## Troubleshooting

### Common Issues

#### Authentication Failures
- **Problem**: 401 Unauthorized errors
- **Solution**: Verify JWT token is valid and not expired
- **Check**: Token format, issuer URL, and client configuration

#### Database Connection Issues
- **Problem**: Application fails to start due to database errors
- **Solution**: Check database configuration and credentials
- **Verify**: H2 console is accessible with provided credentials

#### CORS Issues
- **Problem**: Cross-origin requests blocked
- **Solution**: Configure CORS settings in security configuration

#### Validation Errors
- **Problem**: 400 Bad Request due to invalid data
- **Solution**: Check request payload against validation rules
- **Verify**: Required fields and data formats

## License

This project is licensed under the MIT License.
