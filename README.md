# Employee CRUD Application

A Spring Boot multi-module application demonstrating employee management with OAuth2 authentication using Keycloak as the identity provider.

## Architecture

This project consists of two main modules:

- **employee-service**: RESTful API for employee CRUD operations with OAuth2 resource server security
- **iam-service**: Keycloak-based OAuth2 authorization server for authentication and authorization

## Technology Stack

- **Spring Boot 2.7.5** - Main application framework
- **Spring Security** - Authentication and authorization
- **OAuth2 Resource Server** - JWT token validation
- **Keycloak 18.0.2** - Identity and access management
- **Spring Data JPA** - Database abstraction layer
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **SpringDoc OpenAPI 1.6.12** - API documentation with Swagger UI
- **Maven** - Build and dependency management
- **Java 17** - Runtime environment

## Quick Start

### Prerequisites

- **OpenJDK 17** or higher
  - Recommended: [Amazon Corretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
- **Maven 3.3+**
  - You can use the embedded Maven binaries that your IDE offers, or install your own

### Running the Application

1. **Start the IAM Service (Keycloak Authorization Server)**
   ```bash
   cd iam-service
   mvn spring-boot:run
   ```
   The Keycloak server will start on `http://localhost:8081`

2. **Start the Employee Service**
   ```bash
   cd employee-service
   mvn spring-boot:run
   ```
   The employee service will start on `http://localhost:8080`

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
├── employee-service/          # Employee management API
│   ├── src/main/java/com/employee/
│   │   ├── controllers/       # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── entities/         # JPA entities
│   │   ├── enums/            # Enumeration classes
│   │   ├── exceptions/       # Custom exceptions
│   │   ├── repositories/     # JPA repositories
│   │   └── services/         # Business logic
│   └── EmployeeCrud.postman_collection.json
├── iam-service/              # Keycloak authorization server
│   ├── src/main/resources/
│   │   ├── keycloak-server.json
│   │   └── baeldung-realm.json
├── pom.xml                   # Parent POM
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
mvn clean install
```

### Run tests
```bash
mvn test
```

### Check for dependency updates
```bash
mvn versions:display-property-updates
```

### Update dependency versions
```bash
mvn versions:display-dependency-updates
mvn versions:use-latest-releases
```

## Database

The application uses H2 in-memory database for development. You can access the H2 console:

- **Employee Service**: http://localhost:8080/h2-console
- **IAM Service**: http://localhost:8081/h2-console

Default connection details:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## Configuration

### Employee Service Port
- Default: `8080`
- Can be overridden via: `--server.port=8080`

### IAM Service Port
- Default: `8081`
- Can be overridden via: `--server.port=8081`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

## License

This project is licensed under the MIT License.
