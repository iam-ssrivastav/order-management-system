package com.enterprise.order.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for Order Service.
 * Configures JWT Bearer authentication scheme for API documentation.
 * 
 * @author Shivam Srivastav
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Order Service API", version = "1.0", description = "Order management API with Role-Based Access Control (RBAC). "
        +
        "Login via API Gateway to obtain JWT token with roles. " +
        "ADMIN role required for order cancellation."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "JWT token obtained from API Gateway login endpoint. Token contains user roles (USER/ADMIN).")
public class OpenApiConfig {
}
