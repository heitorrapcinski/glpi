package com.glpi.problem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the Problem Service.
 * Requirements: 28.1, 28.2
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI problemServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GLPI Problem Service API")
                        .description("ITIL Problem management")
                        .version("1.0.0-SNAPSHOT"));
    }
}
