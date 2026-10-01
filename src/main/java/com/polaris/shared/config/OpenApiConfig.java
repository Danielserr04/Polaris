package com.polaris.shared.config;

import com.polaris.shared.error.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Documento OpenAPI de Polaris.
 *
 * <p>Seguridad: el esquema Bearer es un requisito global, asi que todo endpoint lo
 * lleva salvo los que se declaran publicos con {@code @SecurityRequirements} vacio
 * (health, registro, login y verificacion: las mismas rutas abiertas que
 * SecurityConfig). Un endpoint nuevo nace protegido, que es lo que hace la cadena
 * de seguridad.
 *
 * <p>Errores: los controllers solo declaran codigo y descripcion de cada respuesta de
 * error; el cuerpo (siempre {@link ErrorResponse}, ver GlobalExceptionHandler) se
 * rellena aqui para no repetir el schema en cada endpoint.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";
    private static final String SCHEMA_ERROR = "ErrorResponse";

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
                                .description("Token propio de Polaris, emitido al terminar el login de Google "
                                        + "o en POST /api/auth/login")))
                .security(List.of(new SecurityRequirement().addList(ESQUEMA_JWT)));
    }

    /**
     * Completa las respuestas de error: les pone el cuerpo {@link ErrorResponse} y
     * anade el 401 a todo endpoint protegido (SecurityConfig lo devuelve sin token o
     * con un token invalido, antes de llegar al controller).
     */
    @Bean
    public OpenApiCustomizer respuestasDeError() {
        return openApi -> {
            Components componentes = openApi.getComponents();
            ModelConverters.getInstance().readAll(ErrorResponse.class)
                    .forEach(componentes::addSchemas);

            Schema<?> referencia = new Schema<>().$ref("#/components/schemas/" + SCHEMA_ERROR);

            openApi.getPaths().values().forEach(ruta -> ruta.readOperations().forEach(operacion -> {
                if (esProtegida(operacion)) {
                    operacion.getResponses().computeIfAbsent("401", codigo -> new ApiResponse()
                            .description("Falta el token o no es valido"));
                }
                operacion.getResponses().forEach((codigo, respuesta) -> {
                    if ((codigo.startsWith("4") || codigo.startsWith("5")) && sinCuerpoPropio(respuesta)) {
                        respuesta.setContent(new Content().addMediaType("application/json",
                                new MediaType().schema(referencia)));
                    }
                });
            }));
        };
    }

    /**
     * Springdoc rellena una respuesta declarada sin contenido con el tipo que devuelve el
     * metodo (bajo el tipo de medio comodin), que en un error seria mentira. Un contenido
     * declarado a mano lleva un tipo concreto (application/json) y se respeta.
     */
    private boolean sinCuerpoPropio(ApiResponse respuesta) {
        return respuesta.getContent() == null || respuesta.getContent().containsKey("*/*");
    }

    /** Sin requisitos propios hereda el global (protegida); una lista vacia lo anula (publica). */
    private boolean esProtegida(Operation operacion) {
        return operacion.getSecurity() == null || !operacion.getSecurity().isEmpty();
    }
}
