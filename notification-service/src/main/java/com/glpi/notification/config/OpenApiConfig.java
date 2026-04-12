package com.glpi.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the Notification Service.
 * Requirements: 28.1, 28.2
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GLPI Notification Service API")
                        .description("Event-driven notification management")
                        .version("1.0.1-SNAPSHOT"));
    }
}
