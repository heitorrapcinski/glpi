package com.glpi.sla.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3.0 configuration for the SLA Service.
 * Exposes API docs at /v3/api-docs and Swagger UI at /swagger-ui.html.
 * Requirements: 28.1, 28.2
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GLPI SLA Service API",
                version = "1.0.1",
                description = "SLA/OLA definitions, calendars, business-hours computation, and escalation scheduling",
                contact = @Contact(name = "GLPI Backend Team")
        ),
        servers = {
                @Server(url = "http://localhost:8086", description = "Local development"),
                @Server(url = "http://api-gateway:8080", description = "Via API Gateway")
        }
)
public class OpenApiConfig {
    // SpringDoc auto-configures /v3/api-docs and /swagger-ui.html via application.yml
}
