package com.medifit.ratings.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Ratings API",
        description = "Survey processing management"
    )
)
@SecurityScheme(name = "jwt", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT")
class OpenApiConfig
