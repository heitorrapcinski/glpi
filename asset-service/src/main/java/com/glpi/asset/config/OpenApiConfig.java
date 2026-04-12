package com.glpi.asset.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the Asset Service.
 * Requirements: 28.1, 28.2
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assetServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GLPI Asset Service API")
                        .description("CMDB asset and license management")
                        .version("1.0.1-SNAPSHOT"));
    }
}
