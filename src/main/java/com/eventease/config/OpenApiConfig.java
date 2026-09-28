package com.eventease.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventEaseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventEase — College Event Registration System API")
                        .description("REST API documentation for EventEase backend: creating events, registering students, capacity enforcement, viewing participants, and cancellations.")
                        .version("1.0.0")
                        .contact(new Contact().name("EventEase Team")));
    }
}
