package com.cybertec.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        final String bearerSchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info().title("CyberTec API").version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    bearerSchemeName,
                    new SecurityScheme()
                        .name(bearerSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );
    }
}
