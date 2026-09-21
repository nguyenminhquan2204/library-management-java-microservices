package com.javamicroservices.employeeservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "Employee Service API",
        version = "1.0.0",
        description = """
                REST API for managing employees in the Java Microservices system.

                This service provides APIs for:
                - Creating employees
                - Updating employee information
                - Retrieving employee details
                - Listing employees
                - Deleting employees

                The API is designed following RESTful principles.
                """,
        contact = @Contact(
            name = "Java Microservices Team",
            email = "support@example.com",
            url = "https://example.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        ),
        termsOfService = "https://example.com/terms"
    )
)
public class OpenApiConfig {
}