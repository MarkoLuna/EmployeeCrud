# IAM Service - Keycloak Authorization Server

An embedded Keycloak authorization server running within a Spring Boot application, providing OAuth2 authentication and authorization services for the Employee CRUD application.

## Overview

This service embeds Keycloak 18.0.2 as an Identity and Access Management (IAM) solution, offering:

- **OAuth2 Authorization Server** - Issues JWT tokens for client applications
- **User Authentication** - Secure login and user management
- **Role-based Access Control** - Fine-grained authorization policies
- **Single Sign-On (SSO)** - Centralized authentication across multiple services
- **Admin Console** - Web-based administration interface

## Technology Stack

- **Keycloak 18.0.2** - Open-source IAM solution
- **Spring Boot 2.7.5** - Application framework
- **Spring Data JPA** - Database persistence
- **H2 Database** - In-memory database for user/realm data
- **Java 17** - Runtime environment

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.3+

### Running the Service

1. **Build the application**
   ```bash
   ./mvnw clean install
   ```

2. **Start the Keycloak server**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Verify the service is running**
   ```bash
   curl http://localhost:8083/api/healthcheck
   ```

### Access Points

- **Keycloak Admin Console**: http://localhost:8083/auth/
- **Health Check**: http://localhost:8083/api/healthcheck
- **Server Info**: http://localhost:8083/auth/realms/master/.well-known/openid_configuration

## Default Users

The service comes with pre-configured test users:

| Email | Password | Role |
|-------|----------|------|
| john@test.com | 123 | User |
| mike@other.com | pass | User |

**Note**: These credentials are for development/testing purposes only.

## Configuration

### Server Configuration

- **Port**: 8083 (can be overridden with `--server.port=8083`)
- **Context Path**: `/auth`
- **Database**: H2 in-memory database

### Realm Configuration

The service uses a pre-configured realm defined in:
- `src/main/resources/baeldung-realm.json`

Key realm settings:
- **Realm Name**: baeldung
- **Enabled**: true
- **SSL Required**: none (development mode)
- **Access Token Lifespan**: 300 seconds (5 minutes)

## Integration with Employee Service

This IAM service is designed to work with the `employee-service` module:

1. The employee service acts as an OAuth2 **Resource Server**
2. This IAM service acts as the OAuth2 **Authorization Server**
3. JWT tokens issued by Keycloak are validated by the employee service
4. All employee service endpoints require valid JWT tokens

## API Endpoints

### OAuth2 Token Endpoints

- **Token Endpoint**: `POST /auth/realms/baeldung/protocol/openid-connect/token`
- **Authorization Endpoint**: `GET /auth/realms/baeldung/protocol/openid-connect/auth`
- **Logout Endpoint**: `POST /auth/realms/baeldung/protocol/openid-connect/logout`
- **Introspection Endpoint**: `POST /auth/realms/baeldung/protocol/openid-connect/token/introspect`

### Admin API

Keycloak provides a comprehensive REST API for administrative operations:
- **Base URL**: `http://localhost:8083/auth/admin/realms/baeldung`
- **Documentation**: [Keycloak Admin REST API](https://www.keycloak.org/docs-api/18.0/rest-api/)

## Development Guide

### Obtaining Access Tokens

#### Using curl
```bash
curl -X POST "http://localhost:8083/auth/realms/baeldung/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=john@test.com&password=123&client_id=employee-client"
```

#### Using Postman
1. Set Authorization Type to "OAuth 2.0"
2. Configure Token URL: `http://localhost:8083/auth/realms/baeldung/protocol/openid-connect/token`
3. Add credentials and client information
4. Click "Get New Access Token"

### Customization

#### Custom Login Pages
- [Create custom login page guide](https://www.baeldung.com/keycloak-custom-login-page)
- Templates location: `src/main/resources/theme/`

#### Custom Authentication Flows
- [Authentication flows documentation](https://www.keycloak.org/docs/latest/server_admin/index.html#_authentication-flows)
- Configure via Admin Console: Realm Settings → Authentication Flows

## Troubleshooting

### Common Issues

#### Invalid Redirect URI
- **Problem**: `invalid_parameter: redirect_uri`
- **Solution**: Add your redirect URI to Valid Redirect URIs in client configuration
- [Fix guide](https://stackoverflow.com/questions/45352880/keycloak-invalid-parameter-redirect-uri)

#### CORS Issues
- **Problem**: Cross-origin requests blocked
- **Solution**: Configure Web Origins in client settings

### Database Access

Access the H2 console for debugging:
- **URL**: http://localhost:8083/h2-console
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (empty)

## Resources and References

### Official Documentation
- [Keycloak Official Guides](https://www.keycloak.org/guides)
- [Keycloak Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/)
- [Spring Security OAuth Resource Server](https://www.baeldung.com/spring-security-oauth-resource-server)

### Code Examples
- [Original Tutorial](https://www.baeldung.com/keycloak-embedded-in-spring-boot-app)
- [Full Example Code](https://github.com/Baeldung/spring-security-oauth/tree/master/oauth-resource-server)

### Additional Resources
- [Understanding Realms in Spring Security](https://stackoverflow.com/questions/16186834/whats-the-meaning-of-realm-in-spring-security)
- [Postman OAuth2 Setup Guide](https://sis-cc.gitlab.io/dotstatsuite-documentation/configurations/authentication/token-in-postman/)
- [Keycloak REST API Documentation](https://www.keycloak.org/docs-api/18.0/rest-api/)

## Production Considerations

For production deployment, consider:

1. **External Database**: Replace H2 with PostgreSQL, MySQL, or other production database
2. **HTTPS**: Enable SSL/TLS for all communications
3. **Secrets Management**: Use secure credential storage instead of hardcoded values
4. **Clustering**: Configure Keycloak clustering for high availability
5. **Backup Strategy**: Regular backups of realm configurations and user data

## License

This project is licensed under the MIT License.