package com.monika.studentmanagementapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Title and description shown at the top of Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentApiInfo() {
        return new OpenAPI().info(new Info()
                .title("Student Management API")
                .version("1.0.0")
                .description("REST API to create, retrieve, update and delete student records. "
                        + "Data is stored in memory and resets when the application restarts."));
    }
}
