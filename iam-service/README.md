# Keycloak Embedded in a Spring Boot Application
Keycloak is an open-source Identity and Access Management solution administered by RedHat and developed in Java by JBoss.

## Run
```bash
mvn clean install
```

and
```bash
mvn spring-boot:run
```

## Health check path
```bash
curl http://localhost:8083/api/healthcheck
```

App Url: http://localhost:8083/auth/

## Users
- john@test.com / 123
- mike@other.com / pass

## Extra info
Original [Url](https://www.baeldung.com/keycloak-embedded-in-spring-boot-app)

Read more info in [Here](https://www.keycloak.org/guides)

Full Example [Here](https://www.baeldung.com/spring-security-oauth-resource-server)
Code [Here](https://github.com/Baeldung/spring-security-oauth/tree/master/oauth-resource-server)

- Authentication flows [here](https://www.keycloak.org/docs/latest/server_admin/index.html#_authentication-flows)
- How to get an Access Token in Postman [here](https://sis-cc.gitlab.io/dotstatsuite-documentation/configurations/authentication/token-in-postman/#:~:text=Navigate%20to%20the%20Postman%20Authorization,(Keycloak%20in%20our%20case).)
- Realm [here](https://stackoverflow.com/questions/16186834/whats-the-meaning-of-realm-in-spring-security)

## useful links

- Fix invalid redirect uri [here](https://stackoverflow.com/questions/45352880/keycloak-invalid-parameter-redirect-uri)
- [Create custom login páge](https://www.baeldung.com/keycloak-custom-login-page)
- Keycloak Api [here](https://www.keycloak.org/docs-api/12.0/rest-api/)