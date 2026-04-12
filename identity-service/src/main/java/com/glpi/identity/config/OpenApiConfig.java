package com.glpi.identity.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3.0 configuration for the Identity Service.
 * Exposes API docs at /v3/api-docs and Swagger UI at /swagger-ui.html.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GLPI Identity Service API",
                version = "1.0.1",
                description = "Identity & Access Management: users, entities, profiles, groups, authentication, JWT, and 2FA",
                contact = @Contact(name = "GLPI Backend Team")
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Local development"),
                @Server(url = "http://api-gateway:8080", description = "Via API Gateway")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "RS256-signed JWT access token. Obtain via POST /auth/login."
)
public class OpenApiConfig {
    // SpringDoc auto-configures /v3/api-docs and /swagger-ui.html via application.yml
}
