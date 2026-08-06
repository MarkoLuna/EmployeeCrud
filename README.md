# Employee Management System

A distributed system for managing employee records, consisting of multiple microservices built with Spring Boot, Keycloak, and PostgreSQL.

## Services

- **Employee Service**: Manages employee data (CRUD operations).
- **IAM Service**: Handles identity and access management, acting as a client to Keycloak.

## Getting Started

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

#### Employee Service
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

#### IAM Service
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/v3/api-docs

### Default Users

The following users are pre-configured in the `baeldung` realm:

- **John Doe** (`john@test.com` / `123`): Has **`manage-users`**, **`view-users`**, and **`query-users`** roles. Authorized to perform all user management and employee CRUD operations.
- **Mike Smith** (`mike@other.com` / `123`): Has **`user`** role. Authorized for employee operations but restricted from user management.

### Testing APIs

#### Bruno (Recommended)

The project includes a complete [Bruno](https://www.usebruno.com/) collection for testing all microservices.

1.  **Setup**:
    *   Install the Bruno Desktop App.
    *   Open the folder `.bruno` from the project root in Bruno.
    *   Select the **Local** environment in the top right corner.

2.  **Authentication**:
    *   Run the `IAM/Get Token` (John) or `IAM/Get Token Mike` request first.
    *   The `auth_token` variable will be automatically updated for all other requests.

## Project Structure

```
EmployeeCrud/
├── .bruno/                   # Bruno API Collection
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
├── iam-service/              # OAuth2 client service
│   ├── src/main/java/com/authserver/
│   │   ├── config/           # Configuration classes
│   │   ├── controllers/      # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── service/          # User management logic
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

- `GET /employees/{page}/{size}` - Retrieve paged employees
- `GET /employees/{id}` - Retrieve employee by ID
- `POST /employees` - Create new employee
- `PUT /employees/{id}` - Update existing employee
- `DELETE /employees/{id}` - Delete employee

The IAM service provides endpoints for user management:

- `POST /api/users` - Create a new user
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `PUT /api/users/{id}` - Update user details and roles
- `DELETE /api/users/{id}` - Delete a user

## Security Configuration

- **OAuth2 Resource Server**: Both services validate JWT tokens issued by Keycloak
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

## Service Port Mappings

- **Keycloak**: http://localhost:8083 (Docker container)
- **IAM Service**: http://localhost:8082 (local process)
- **Employee Service**: http://localhost:8080 (local process)
- **PostgreSQL**: localhost:5432 (Docker container)

