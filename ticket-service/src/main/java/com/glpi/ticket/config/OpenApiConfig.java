package com.glpi.ticket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the Ticket Service.
 * Requirements: 28.1, 28.2
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ticketServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GLPI Ticket Service API")
                        .description("ITIL Incident and Service Request management")
                        .version("1.0.1"));
    }
}
