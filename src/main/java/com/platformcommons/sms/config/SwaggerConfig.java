package com.platformcommons.sms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI studentManagementOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Student Management System API")
                        .description("""
                                REST API for managing student admissions, courses, and enrollments.
                                
                                **Admin Operations:**
                                - Register students
                                - Create courses
                                - Enroll / unenroll students into courses
                                - Search students by name or course
                                
                                **Student Operations:**
                                - View and update profile
                                - Search courses by name or topic
                                - Leave a course
                                """)
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your raw JWT string below. Do NOT prefix it with 'Bearer ' manually.")
                        ));
    }
}
