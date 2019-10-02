package com.employee;

import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SpringFoxConfig {

    /*
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                // .paths(paths())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(getApiInfo())
                .securitySchemes(Collections.singletonList(apiKey()))
                .securityContexts(Collections.singletonList(securityContext()))
                .useDefaultResponseMessages(false);
    }
    */

    private Predicate<String> paths() {
        return or(
                // regex("/oauth/token"),
                regex("/employees.*")
        );
    }

    private ApiInfo getApiInfo() {
        return new ApiInfo(
                "Employee Crud",
                "Basic Crud of Employees",
                "1.0.0",
                "",
                new Contact("Jose Marcos Luna","","josemarcosluna9@gmail.com"),
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                Collections.emptyList()
        );
    }

    /**
    private ApiKey apiKey() {
        return new ApiKey("Authentication", "Authorization", "Authorization");
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEmployees");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference("Authentication", authorizationScopes));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/employees.*"))
                .build();
    }*/

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(paths())
                // .paths(PathSelectors.any())
                .build()
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Arrays.asList(securitySchema()))
                // .securitySchemes(Arrays.asList(securitySchema(), apiKey(), apiCookieKey()))
                .apiInfo(getApiInfo());
    }

    @Value("${config.oauth2.accessTokenUri}")
    private String accessTokenUri;

    /**
    public static final String securitySchemaOAuth2 = "oauth2schema";
    public static final String authorizationScopeGlobal = "global";
    public static final String authorizationScopeGlobalDesc ="accessEverything";
     */

    @Bean
    public SecurityScheme apiKey() {
        return new ApiKey(HttpHeaders.AUTHORIZATION, "apiKey", "header");
    }

    /**
    @Bean
    public SecurityScheme apiCookieKey() {
        return new ApiKey(HttpHeaders.COOKIE, "apiKey", "cookie");
    }
     */

    private OAuth securitySchema() {

        /**
        List<AuthorizationScope> authorizationScopeList = new ArrayList<>();
        authorizationScopeList.add(new AuthorizationScope("read", "read all"));
        authorizationScopeList.add(new AuthorizationScope("write", "access all"));
         */

        List<GrantType> grantTypes = new ArrayList<>();
        GrantType passwordCredentialsGrant = new ResourceOwnerPasswordCredentialsGrant(accessTokenUri);
        grantTypes.add(passwordCredentialsGrant);

        return new OAuth("oauth2", Collections.emptyList(), grantTypes);
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        /**
        final AuthorizationScope[] authorizationScopes = new AuthorizationScope[3];
        authorizationScopes[0] = new AuthorizationScope("read", "read all");
        authorizationScopes[1] = new AuthorizationScope("trust", "trust all");
        authorizationScopes[2] = new AuthorizationScope("write", "write all");
         */

        return Collections.singletonList(new SecurityReference("oauth2", new AuthorizationScope[0]));
    }

    @Bean
    public SecurityConfiguration security() {
        return new SecurityConfiguration("client", "secret", "", "",
                "Bearer access token", ApiKeyVehicle.HEADER, HttpHeaders.AUTHORIZATION,"");
    }
}
