package com.glpi.knowledge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the Knowledge Service.
 * Requirements: 28.1, 28.2
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI knowledgeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GLPI Knowledge Service API")
                        .description("Knowledge base article and category management")
                        .version("1.0.1-SNAPSHOT"));
    }
}
