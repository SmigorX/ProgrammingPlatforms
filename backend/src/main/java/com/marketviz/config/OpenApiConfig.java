package com.marketviz.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the OpenAPI specification with project metadata and a JWT bearer
 * security scheme so that Swagger UI can authenticate against the API.
 *
 * <p>Swagger UI is available at {@code /swagger-ui.html}; the raw OpenAPI JSON
 * at {@code /api-docs}.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title       = "MarketViz API",
        version     = "0.1.0",
        description = "REST API for the MarketViz market data visualization platform"
))
@SecurityScheme(
        name        = "bearerAuth",
        type        = SecuritySchemeType.HTTP,
        scheme      = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {}
