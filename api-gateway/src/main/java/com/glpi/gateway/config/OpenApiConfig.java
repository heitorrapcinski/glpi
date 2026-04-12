package com.glpi.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc WebFlux configuration for the API Gateway.
 * Exposes a unified Swagger UI aggregating specs from all downstream services.
 * The downstream service URLs are configured in application.yml under springdoc.swaggerui.urls.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GLPI Microservices API")
                        .description("Unified API documentation for all GLPI microservices. " +
                                "Use the dropdown to switch between service specs.")
                        .version("1.0.0"));
    }
}
