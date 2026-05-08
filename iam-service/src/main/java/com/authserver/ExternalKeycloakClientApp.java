package com.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.authserver.config.KeycloakClientConfig;

@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
@EnableConfigurationProperties({ KeycloakClientConfig.class })
public class ExternalKeycloakClientApp {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalKeycloakClientApp.class);

    public static void main(String[] args) {
        LOG.info("Starting IAM Service as OAuth2 Client for External Keycloak");
        SpringApplication.run(ExternalKeycloakClientApp.class, args);
    }
}
