# Employee CRUD Application

A Spring Boot multi-module application demonstrating employee management with OAuth2 authentication using Keycloak as the identity provider.

## Architecture

This project consists of two main modules:

- **employee-service**: RESTful API for employee CRUD operations with OAuth2 resource server security
- **iam-service**: Keycloak-based OAuth2 authorization server for authentication and authorization

## Technology Stack

- **Spring Boot 3.4.0** - Main application framework
- **Spring Security** - Authentication and authorization
- **OAuth2 Resource Server** - JWT token validation
- **Keycloak 24.0.4** - Identity and access management (external Docker container)
- **Spring Data JPA** - Database abstraction layer
- **PostgreSQL** - Production database (Docker)
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **SpringDoc OpenAPI** - API documentation with Swagger UI
- **Docker & Docker Compose** - Containerization and orchestration
- **Maven** - Build and dependency management
- **Java 21** - Runtime environment

## Quick Start

### Prerequisites

- **OpenJDK 21** or higher
  - Recommended: [Amazon Corretto 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html)
- **Maven 3.3+**
  - You can use the embedded Maven binaries that your IDE offers, or install your own
- **Docker & Docker Compose**
  - Required for running Keycloak and PostgreSQL containers

### Running the Application

#### Recommended Setup: Local Services + Docker Dependencies

1. **Start Docker dependencies (Keycloak and PostgreSQL)**
   ```bash
   cd docker
   docker-compose up -d
   ```
   This will start:
   - **Keycloak**: http://localhost:8083 (identity provider)
   - **PostgreSQL**: localhost:5432 (database)

2. **Start the IAM Service**
   ```bash
   cd iam-service
   ./mvnw spring-boot:run
   ```
   The IAM service will start on `http://localhost:8082`

3. **Start the Employee Service**
   ```bash
   cd employee-service
   ./mvnw spring-boot:run
   ```
   The employee service will start on `http://localhost:8080`

4. **Stop dependencies**
   ```bash
   cd docker
   docker-compose down
   ```

#### Alternative: Full Docker Setup

If you prefer to run everything in containers, you can modify the Docker Compose file to include the application services.

### Architecture Overview

```
Local Services                    Docker Dependencies
┌─────────────────┐              ┌─────────────────┐
│  IAM Service    │◄────────────►│    Keycloak     │
│  (localhost:8082) │              │ (localhost:8083)│
└─────────────────┘              └─────────────────┘
         │                               │
         ▼                               ▼
┌─────────────────┐              ┌─────────────────┐
│ Employee Service│◄────────────►│   PostgreSQL    │
│ (localhost:8080) │              │ (localhost:5432)│
└─────────────────┘              └─────────────────┘
```

### API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Testing with Postman

1. Import the provided Postman collection:
   - File: `employee-service/EmployeeCrud.postman_collection.json`
   
2. Authentication flow:
   - Select an environment in Postman
   - Run the Login request first to obtain an OAuth2 token
   - The token will be automatically stored for subsequent requests

## Project Structure

```
EmployeeCrud/
├── docker/                   # Docker dependencies
│   ├── docker-compose.yml   # Keycloak and PostgreSQL services
│   └── baeldung-realm.json  # Keycloak realm configuration
├── employee-service/         # Employee management API
│   ├── src/main/java/com/employee/
│   │   ├── controllers/       # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── entities/         # JPA entities
│   │   ├── enums/            # Enumeration classes
│   │   ├── exceptions/       # Custom exceptions
│   │   ├── repositories/     # JPA repositories
│   │   └── services/         # Business logic
│   └── EmployeeCrud.postman_collection.json
├── iam-service/              # OAuth2 client service
│   ├── src/main/java/com/authserver/
│   │   ├── config/           # Configuration classes
│   │   ├── controllers/      # Health check controllers
│   │   └── ExternalKeycloakClientApp.java
│   ├── src/main/resources/
│   │   └── application.yml   # Single configuration for all environments
│   └── Dockerfile
├── pom.xml                   # Parent POM
├── endpoints.http            # API testing endpoints
└── README.md
```

## API Endpoints

The employee service provides the following endpoints (all require OAuth2 authentication):

- `GET /api/employees` - Retrieve all employees
- `GET /api/employees/{id}` - Retrieve employee by ID
- `POST /api/employees` - Create new employee
- `PUT /api/employees/{id}` - Update existing employee
- `DELETE /api/employees/{id}` - Delete employee

## Security Configuration

- **OAuth2 Resource Server**: Employee service validates JWT tokens issued by Keycloak
- **Keycloak Realm**: Pre-configured realm for employee management
- **Token-based Authentication**: All API endpoints require valid JWT tokens

## Development Commands

### Build the entire project
```bash
./mvnw clean install
```

### Run tests
```bash
./mvnw test
```

### Check for dependency updates
```bash
./mvnw versions:display-property-updates
```

### Update dependency versions
```bash
./mvnw versions:display-dependency-updates
./mvnw versions:use-latest-releases
```

## Database

### Docker Environment
- **PostgreSQL**: Used in Docker Compose setup
  - Host: localhost:5432
  - Database: employee_db
  - Username: employee_user
  - Password: employee_pass

### Local Development
- **H2 Database**: In-memory database for local development
  - **Employee Service**: http://localhost:8080/h2-console
  - **IAM Service**: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

## Service Port Mappings

### Current Architecture
- **Keycloak**: http://localhost:8083 (Docker container)
- **IAM Service**: http://localhost:8082 (local process)
- **Employee Service**: http://localhost:8080 (local process)
- **PostgreSQL**: localhost:5432 (Docker container)

## Configuration

### Single Configuration File
The IAM service uses a single `application.yml` configuration file that works for all environments:

- **Keycloak URL**: http://keycloak:8080 (Docker network)
- **Database**: PostgreSQL (Docker container)
- **Logging**: DEBUG level

### Docker Dependencies Configuration

#### Keycloak Configuration
- **Container Name**: keycloak
- **Image**: quay.io/keycloak/keycloak:24.0.4
- **Port**: 8083:8080 (host:container)
- **Realm**: baeldung (auto-imported)
- **Admin Credentials**: admin/admin

#### PostgreSQL Configuration
- **Container Name**: postgres
- **Image**: postgres:15
- **Port**: 5432:5432 (host:container)
- **Database**: employee_db
- **Credentials**: employee_user/employee_pass

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

## License

This project is licensed under the MIT License.
