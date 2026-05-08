package com.authserver.config;

import lombok.Data;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.ws.rs.client.ClientBuilder;

@Configuration
@Data
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminConfig {
    
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    
    @Bean
    public Keycloak keycloakAdminClient(BearerTokenInterceptor bearerTokenInterceptor) {
        // Create a ResteasyClient with the BearerTokenInterceptor
        ResteasyClient resteasyClient = (ResteasyClient) ClientBuilder.newBuilder()
                .register(bearerTokenInterceptor)
                .build();

        // KeycloakBuilder requires at least a username or a token during initialization.
        // We provide a placeholder token because the BearerTokenInterceptor will 
        // dynamically inject the correct token from the SecurityContext for every request.
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorization("placeholder-token")
                .resteasyClient(resteasyClient)
                .build();
    }
    
    @Bean
    public RealmResource keycloakRealmResource(Keycloak keycloak) {
        return keycloak.realm(realm);
    }
}
