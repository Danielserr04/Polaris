package com.polaris.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El documento OpenAPI completo (todas las rutas con sus anotaciones) se valida
 * arrancando la app; aqui solo la logica propia de OpenApiConfig, sin contexto.
 */
class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();
    private OpenAPI openApi;

    @BeforeEach
    void prepararDocumento() {
        openApi = new OpenAPI().components(new Components()).paths(new Paths());
    }

    @Test
    void elEsquemaBearerEsUnRequisitoGlobal() {
        OpenAPI base = config.polarisOpenApi();

        assertThat(base.getComponents().getSecuritySchemes()).containsKey("bearer-jwt");
        assertThat(base.getSecurity()).hasSize(1);
        assertThat(base.getSecurity().get(0)).containsKey("bearer-jwt");
    }

    @Test
    void unEndpointProtegidoRecibeElCuatrocientosUnoConElSchemaDeError() {
        Operation protegida = operacion("/api/algo", new ApiResponses().addApiResponse("200", new ApiResponse()));

        config.respuestasDeError().customise(openApi);

        ApiResponse respuesta = protegida.getResponses().get("401");
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ErrorResponse");
        assertThat(openApi.getComponents().getSchemas()).containsKey("ErrorResponse");
    }

    @Test
    void unEndpointPublicoNoRecibeElCuatrocientosUno() {
        Operation publica = operacion("/api/publica", new ApiResponses().addApiResponse("200", new ApiResponse()));
        publica.setSecurity(List.of());

        config.respuestasDeError().customise(openApi);

        assertThat(publica.getResponses()).doesNotContainKey("401");
    }

    @Test
    void unCuatrocientosUnoDeclaradoAManoNoSeSustituye() {
        Operation login = operacion("/api/login", new ApiResponses()
                .addApiResponse("401", new ApiResponse().description("Credenciales incorrectas")));
        login.setSecurity(List.of());

        config.respuestasDeError().customise(openApi);

        assertThat(login.getResponses().get("401").getDescription()).isEqualTo("Credenciales incorrectas");
    }

    @Test
    void elCuerpoQueSpringdocPonePorDefectoEnUnErrorSeCambiaPorErrorResponse() {
        Operation operacion = operacion("/api/algo", new ApiResponses()
                .addApiResponse("404", new ApiResponse().description("No existe").content(
                        new Content().addMediaType("*/*", new MediaType().schema(new Schema<>().$ref("#/x/Otro")))))
                .addApiResponse("200", new ApiResponse().content(
                        new Content().addMediaType("*/*", new MediaType().schema(new Schema<>().$ref("#/x/Ok"))))));

        config.respuestasDeError().customise(openApi);

        assertThat(operacion.getResponses().get("404").getContent()).containsOnlyKeys("application/json");
        assertThat(operacion.getResponses().get("404").getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ErrorResponse");
        // el 2xx no se toca
        assertThat(operacion.getResponses().get("200").getContent()).containsOnlyKeys("*/*");
    }

    @Test
    void unContenidoDeErrorDeclaradoAManoSeRespeta() {
        Operation operacion = operacion("/health", new ApiResponses()
                .addApiResponse("503", new ApiResponse().content(new Content().addMediaType("application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/HealthDto"))))));
        operacion.setSecurity(List.of());

        config.respuestasDeError().customise(openApi);

        assertThat(operacion.getResponses().get("503").getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/HealthDto");
    }

    @Test
    void unRequisitoPropioNoVacioTambienCuentaComoProtegida() {
        Operation operacion = operacion("/api/algo", new ApiResponses());
        operacion.setSecurity(List.of(new SecurityRequirement().addList("bearer-jwt")));

        config.respuestasDeError().customise(openApi);

        assertThat(operacion.getResponses()).containsKey("401");
    }

    @Test
    void todaRespuestaDeclaraLaCabeceraXRequestId() {
        Operation protegida = operacion("/api/algo", new ApiResponses()
                .addApiResponse("200", new ApiResponse())
                .addApiResponse("404", new ApiResponse()));

        config.respuestasDeError().customise(openApi);

        // tambien el 401 que se anade solo
        assertThat(protegida.getResponses()).containsKeys("200", "404", "401");
        protegida.getResponses().values().forEach(respuesta ->
                assertThat(respuesta.getHeaders()).containsKey("X-Request-Id"));
    }

    private Operation operacion(String ruta, ApiResponses respuestas) {
        Operation operacion = new Operation().responses(respuestas);
        openApi.getPaths().addPathItem(ruta, new PathItem().get(operacion));
        return operacion;
    }
}
