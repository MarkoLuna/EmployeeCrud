package com.authserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.keycloak")
@Data
public class SimpleKeycloakConfig {

    private String url;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String adminUser;
    private String adminPassword;
}
