package com.polaris.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    public OpenAPI polarisOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Polaris API")
                        .description("App web personal y modular")
                        .version("v0"))
                // Habilita el boton Authorize de Swagger: se pega ahi el token que
                // devuelve el login de Google y ya viaja en todas las peticiones.
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token propio de Polaris, emitido al terminar el login de Google")));
    }
}
