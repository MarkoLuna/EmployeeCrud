package com.authserver;

import com.authserver.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = { ExternalKeycloakClientApp.class })
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false"
})
public class ContextIntegrationTest {

    @Autowired
    private UserManagementService userManagementService;

    @Test
    public void whenLoadApplication_thenSuccess() {
        // Test passes if Spring context loads successfully
        assertThat(userManagementService).isNotNull();
    }

}
