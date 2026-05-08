# Docker Setup for Employee CRUD Application

This folder contains the Docker configuration for running Keycloak and PostgreSQL as dependencies for the Employee CRUD application.

## Services

### Keycloak
- **Image**: quay.io/keycloak/keycloak:24.0.4
- **Port**: 8083 (host) → 8080 (container)
- **Realm**: baeldung (auto-imported from baeldung-realm.json)
- **Admin Console Credentials**: admin/admin

### PostgreSQL
- **Image**: postgres:15
- **Port**: 5432 (host) → 5432 (container)
- **Database**: employee_db
- **Credentials**: employee_user/employee_pass

## Pre-configured Users (baeldung realm)

The following users are available for testing role-based access control:

| Username | Password | Roles | Permissions |
|----------|----------|-------|-------------|
| **john@test.com** | 123 | `manage-users`, `user` | Full administrative access to IAM and Employee services. |
| **mike@other.com** | 123 | `user` | Authorized for employee operations only. Forbidden from user management. |

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

# Create new user (requires manage-users role)
curl -X POST "http://localhost:8082/api/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "firstName": "New",
    "lastName": "User",
    "email": "newuser@example.com",
    "password": "password123",
    "enabled": true
  }'
```

## Service Endpoints

### IAM Service (Local)
- **Health Check**: http://localhost:8082/actuator/health
- **Swagger UI**: http://localhost:8082/swagger-ui.html

### Employee Service (Local)
- **Health Check**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Keycloak (Docker)
- **Admin Console**: http://localhost:8083
- **Token Endpoint**: http://localhost:8083/realms/baeldung/protocol/openid-connect/token
- **JWKS Endpoint**: http://localhost:8083/realms/baeldung/protocol/openid-connect/certs

### PostgreSQL (Docker)
- **Host**: localhost
- **Port**: 5432
- **Database**: employee_db

## Troubleshooting

### Reset Environment
If you need a fresh start with all data cleared:
```bash
docker-compose down -v
docker-compose up -d
```

### Check Logs
```bash
docker-compose logs -f keycloak
```

## Security Notes

- **Client Secret**: `newClientSecret` (for development only)
- **Token Expiry**: 5 minutes for access tokens, 30 minutes for refresh tokens
- **HTTPS**: Disabled for local development
