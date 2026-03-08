package com.banking.channelconfig.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Channel Configurations Service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI channelConfigOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Channel Configurations Service API")
                        .description("Feature flag service with ACL (Access Control List) support. " +
                                   "Manages feature flags and user access permissions.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Banking Platform Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Local development server")
                ));
    }
}
