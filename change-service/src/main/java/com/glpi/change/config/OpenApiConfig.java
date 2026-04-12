package com.glpi.change.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the Change Service.
 * Requirements: 28.1, 28.2
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI changeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GLPI Change Service API")
                        .description("ITIL Change management")
                        .version("1.0.0"));
    }
}
