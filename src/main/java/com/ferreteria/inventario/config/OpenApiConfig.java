package com.ferreteria.inventario.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI sigefOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SIGEF - API de Inventario de Ferretería")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión de productos, categorías y ventas.
                                Autenticación JWT: ejecute POST /api/auth/login, copie el valor de "token"
                                y péguelo en el botón Authorize (sin escribir la palabra Bearer)."""))
                // Todos los endpoints exigen token por defecto (candado en Swagger)
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pegue solo el token; Swagger agrega 'Bearer ' automáticamente")));
    }
}
