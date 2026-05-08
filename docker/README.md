# Docker Setup for Employee CRUD Application

This folder contains the Docker configuration for running Keycloak and PostgreSQL as dependencies for the Employee CRUD application.

## Services

### Keycloak
- **Image**: quay.io/keycloak/keycloak:24.0.4
- **Port**: 8083 (host) → 8080 (container)
- **Realm**: baeldung (auto-imported from baeldung-realm.json)
- **Admin Credentials**: admin/admin

### PostgreSQL
- **Image**: postgres:15
- **Port**: 5432 (host) → 5432 (container)
- **Database**: employee_db
- **Credentials**: employee_user/employee_pass

## Quick Start

```bash
# Start all dependencies
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs keycloak
docker-compose logs postgres

# Stop all dependencies
docker-compose down
```

## JWT Token Generation Examples

### 1. Get Access Token with curl

```bash
curl -X POST "http://localhost:8083/realms/baeldung/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=newClient&client_secret=newClientSecret&grant_type=password&username=john@test.com&password=123"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJlUktVWG10TFhKMHBBNkxBS29aWko1ZlU0VDhCdmxKdERCb3pXanFFdnhjIn0...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3ZWQ5ZjEzYy1kNGU3LTRkODQtYTNjYy1kODg3OWRmZWExYjMifQ...",
  "scope": "profile"
}
```

### 2. Get Access Token and Extract Token

```bash
# Get token and store in variable
TOKEN=$(curl -s -X POST "http://localhost:8083/realms/baeldung/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=newClient&client_secret=newClientSecret&grant_type=password&username=john@test.com&password=123" | \
  jq -r '.access_token')

echo "Token: $TOKEN"
```

### 3. Use Token for API Requests

```bash
# Get employee list with Bearer token
curl -X GET "http://localhost:8080/employees/0/10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"

# Create new employee
curl -X POST "http://localhost:8080/employees" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "middleInitial": "D",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-01",
    "dateOfEmployment": "2020-01-01"
  }'
```

### 4. Refresh Token

```bash
# Use refresh token to get new access token
curl -X POST "http://localhost:8083/realms/baeldung/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=newClient&client_secret=newClientSecret&grant_type=refresh_token&refresh_token=$REFRESH_TOKEN"
```

### 5. Client Credentials Grant (Service-to-Service)

```bash
# Get token using client credentials
curl -X POST "http://localhost:8083/realms/baeldung/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=newClient&client_secret=newClientSecret&grant_type=client_credentials"
```

### 6. Token Introspection (Validate Token)

```bash
# Validate token
curl -X POST "http://localhost:8083/realms/baeldung/protocol/openid-connect/token/introspect" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=newClient&client_secret=newClientSecret&token=$TOKEN"
```

## Test Users in baeldung Realm

| Username | Password | Roles |
|----------|----------|-------|
| john@test.com | 123 | user, admin |
| jane@test.com | 123 | user |
| admin@test.com | 123 | admin |

## Service Endpoints

### IAM Service (Local)
- **Health Check**: http://localhost:8082/actuator/health
- **Port**: 8082

### Employee Service (Local)
- **API Base**: http://localhost:8080/employees
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Port**: 8080

### Keycloak (Docker)
- **Admin Console**: http://localhost:8083/admin
- **Token Endpoint**: http://localhost:8083/realms/baeldung/protocol/openid-connect/token
- **JWKS Endpoint**: http://localhost:8083/realms/baeldung/protocol/openid-connect/certs
- **Port**: 8083

### PostgreSQL (Docker)
- **Host**: localhost
- **Port**: 5432
- **Database**: employee_db

## Troubleshooting

### Keycloak Issues

```bash
# Check Keycloak logs
docker logs keycloak

# Restart Keycloak
docker-compose restart keycloak

# Rebuild and start
docker-compose up -d --build
```

### Database Issues

```bash
# Check PostgreSQL logs
docker logs postgres

# Connect to database
docker exec -it postgres psql -U employee_user -d employee_db

# Reset database (remove volume)
docker-compose down -v
docker-compose up -d
```

### Token Issues

1. **404 Error**: Check if Keycloak is running and accessible
2. **401 Error**: Verify client credentials and user credentials
3. **Token Expired**: Use refresh token or get new token
4. **Invalid Scope**: Check client configuration in Keycloak

## Development Workflow

1. **Start Dependencies**: `docker-compose up -d`
2. **Start IAM Service**: `cd ../iam-service && ./mvnw spring-boot:run`
3. **Start Employee Service**: `cd ../employee-service && ./mvnw spring-boot:run`
4. **Get Token**: Use curl examples above
5. **Test APIs**: Use token in Authorization header
6. **Stop Dependencies**: `docker-compose down`

## Configuration Files

- **docker-compose.yml**: Service definitions and configurations
- **baeldung-realm.json**: Keycloak realm configuration with users and clients

## Security Notes

- **Client Secret**: `newClientSecret` (for development only)
- **Admin Credentials**: `admin/admin` (for development only)
- **Token Expiry**: 5 minutes for access tokens, 30 minutes for refresh tokens
- **HTTPS**: Disabled for development (enable for production)

## Network Configuration

All services are connected to the `employee-network` Docker network:
- **keycloak**: Container name for internal communication
- **postgres**: Container name for internal communication
- **Local Services**: Connect via localhost:port
