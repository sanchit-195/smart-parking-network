package com.smartparking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 / Swagger documentation configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartParkingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Parking Network REST API")
                        .description("REST API documentation for the Dynamic Smart Parking Network IoT backend. "
                                + "Manages 24 parking slots across Zones A, B, and C, processes real-time ultrasonic sensor readings, "
                                + "calculates occupancy statistics, and maintains activity event logs.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Smart Parking Network Project Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}

