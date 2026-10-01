package com.polaris.shared.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ch.qos.logback.classic.Level;
import com.polaris.shared.testing.CapturaLogs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc standalone con un controller de mentira: se prueba solo la traduccion
 * de excepciones a HTTP, sin Spring Security ni base de datos.
 */
class GlobalExceptionHandlerTest {

    enum Estado { PENDIENTE, VISTA }

    record CuerpoDto(@NotNull Long tituloId, Estado estado, LocalDate fecha, @NotBlank String nombre) {
    }

    record FiltroDto(Estado estado, YearMonth periodo, @NotBlank String texto) {
    }

    @RestController
    @RequestMapping("/prueba")
    static class PruebaController {

        @PostMapping
        String crear(@Valid @RequestBody CuerpoDto dto) {
            return "ok";
        }

        @PostMapping("/lista")
        String lista(@RequestBody List<CuerpoDto> dtos) {
            return "ok";
        }

        @GetMapping("/filtro")
        String filtrar(@Valid FiltroDto filtro) {
            return "ok";
        }

        @GetMapping("/{id}")
        String detalle(@PathVariable Long id) {
            throw new NotFoundException("No encontrado: " + id);
        }

        @GetMapping("/suelto")
        String suelto(@RequestParam Estado estado) {
            return "ok";
        }

        @GetMapping("/unico-jdbc")
        String unicoJdbc() {
            throw new DataIntegrityViolationException("no filtrar esto",
                    new SQLIntegrityConstraintViolationException(
                            "Duplicate entry 'a' for key 'uk_secreto'", "23000", 1062));
        }

        @GetMapping("/unico-hibernate")
        String unicoHibernate() {
            throw new DataIntegrityViolationException("no filtrar esto",
                    new org.hibernate.exception.ConstraintViolationException(
                            "could not execute statement [insert into secreta]",
                            new SQLIntegrityConstraintViolationException(
                                    "Duplicate entry 'a' for key 'uk_secreto'", "23000", 1062),
                            "insert into secreta", "uk_secreto"));
        }

        @GetMapping("/fk")
        String claveAjena() {
            throw new DataIntegrityViolationException("no filtrar esto",
                    new org.hibernate.exception.ConstraintViolationException(
                            "could not execute statement",
                            new SQLIntegrityConstraintViolationException(
                                    "Cannot add or update a child row: a foreign key constraint fails", "23000", 1452),
                            "insert into secreta", "fk_secreta"));
        }

        @GetMapping("/not-null")
        String noNulo() {
            throw new DataIntegrityViolationException("no filtrar esto",
                    new SQLIntegrityConstraintViolationException("Column 'x' cannot be null", "23000", 1048));
        }

        @GetMapping("/sin-causa")
        String sinCausa() {
            throw new DataIntegrityViolationException("sin causa");
        }

        @GetMapping("/inexistente")
        String inexistente() throws Exception {
            throw new NoResourceFoundException(HttpMethod.GET, "inexistente");
        }

        @GetMapping("/roto")
        String roto() {
            throw new IllegalStateException("detalle interno secreto");
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PruebaController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void enumInvalidoEnElCuerpoDevuelve400ConCampoYValores() throws Exception {
        mockMvc.perform(post("/prueba").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloId\":1,\"estado\":\"X\",\"nombre\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/prueba"))
                .andExpect(jsonPath("$.error").value("estado: valor no valido. Valores admitidos: PENDIENTE, VISTA"));
    }

    @Test
    void jsonMalFormadoDevuelve400Generico() throws Exception {
        mockMvc.perform(post("/prueba").contentType(MediaType.APPLICATION_JSON).content("{\"tituloId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El cuerpo de la peticion no es un JSON valido"));
    }

    @Test
    void cuerpoVacioDevuelve400() throws Exception {
        mockMvc.perform(post("/prueba").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Falta el cuerpo de la peticion"));
    }

    @Test
    void textoDondeVaUnNumeroDevuelve400ConElCampo() throws Exception {
        mockMvc.perform(post("/prueba").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloId\":\"abc\",\"nombre\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("tituloId: valor no valido"));
    }

    @Test
    void fechaMalFormadaDevuelve400ConElCampoSinFiltrarClases() throws Exception {
        mockMvc.perform(post("/prueba").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloId\":1,\"fecha\":\"ayer\",\"nombre\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("fecha: valor no valido"))
                .andExpect(jsonPath("$.error").value(not(containsString("java."))));
    }

    @Test
    void enumInvalidoDentroDeUnaListaIndicaLaPosicion() throws Exception {
        mockMvc.perform(post("/prueba/lista").contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"tituloId\":1,\"estado\":\"X\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "[0].estado: valor no valido. Valores admitidos: PENDIENTE, VISTA"));
    }

    @Test
    void queryParamEnumInvalidoEnUnDtoDevuelveMensajeLegible() throws Exception {
        mockMvc.perform(get("/prueba/filtro").param("estado", "X").param("texto", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("estado: valor no valido. Valores admitidos: PENDIENTE, VISTA"));
    }

    @Test
    void queryParamFechaInvalidaEnUnDtoDevuelveMensajeLegible() throws Exception {
        mockMvc.perform(get("/prueba/filtro").param("periodo", "2026-99").param("texto", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("periodo: valor no valido"));
    }

    @Test
    void queryParamSueltoConEnumInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/prueba/suelto").param("estado", "X"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("estado: valor no valido. Valores admitidos: PENDIENTE, VISTA"));
    }

    @Test
    void variableDeRutaNoNumericaDevuelve400() throws Exception {
        mockMvc.perform(get("/prueba/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("id: valor no valido"));
    }

    @Test
    void beanValidationSigueIgualEnElCuerpo() throws Exception {
        mockMvc.perform(post("/prueba").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloId\":1,\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("nombre: must not be blank"));
    }

    @Test
    void beanValidationSigueIgualEnQueryParams() throws Exception {
        mockMvc.perform(get("/prueba/filtro").param("texto", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("texto: must not be blank"));
    }

    @Test
    void notFoundSigueDevolviendo404() throws Exception {
        mockMvc.perform(get("/prueba/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No encontrado: 42"));
    }

    @Test
    void unErrorRealSigueSiendo500SinDetalleInterno() throws Exception {
        mockMvc.perform(get("/prueba/roto"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));
    }

    @Test
    void violacionDeUnicidadJdbcDevuelve409SinFiltrarIndicesNiSql() throws Exception {
        mockMvc.perform(get("/prueba/unico-jdbc"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/prueba/unico-jdbc"))
                .andExpect(jsonPath("$.error").value("El recurso ya existe"))
                .andExpect(jsonPath("$.error").value(not(containsString("uk_secreto"))));
    }

    @Test
    void violacionDeUnicidadEnvueltaPorHibernateDevuelve409() throws Exception {
        mockMvc.perform(get("/prueba/unico-hibernate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El recurso ya existe"));
    }

    @Test
    void violacionDeClaveAjenaNoSeTraduceA409() throws Exception {
        mockMvc.perform(get("/prueba/fk"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));
    }

    @Test
    void violacionNotNullNoSeTraduceA409() throws Exception {
        mockMvc.perform(get("/prueba/not-null"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));
    }

    @Test
    void violacionDeIntegridadSinCausaSqlSigueSiendo500() throws Exception {
        mockMvc.perform(get("/prueba/sin-causa"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void metodoNoSoportadoDevuelve405ConCabeceraAllow() throws Exception {
        mockMvc.perform(delete("/prueba"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Metodo DELETE no soportado en esta ruta. Metodos admitidos: POST"));
    }

    @Test
    void contentTypeNoSoportadoDevuelve415() throws Exception {
        mockMvc.perform(post("/prueba").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value(containsString("Content-Type no soportado: text/plain")));
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/prueba/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }

    @Test
    void parametroObligatorioAusenteDevuelve400() throws Exception {
        mockMvc.perform(get("/prueba/suelto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Falta el parametro obligatorio: estado"));
    }

    @Test
    void elCincuentaYTresRegistraElStacktraceYLosCuatrocientosNo() throws Exception {
        try (CapturaLogs logs = CapturaLogs.de(GlobalExceptionHandler.class)) {
            mockMvc.perform(get("/prueba/roto")).andExpect(status().isInternalServerError());
            mockMvc.perform(get("/prueba/1")).andExpect(status().isNotFound());
            mockMvc.perform(get("/prueba/suelto")).andExpect(status().isBadRequest());
            mockMvc.perform(get("/prueba/unico-jdbc")).andExpect(status().isConflict());

            // 500: un solo ERROR, con la excepcion completa y sin la query string.
            assertThat(logs.eventos(Level.ERROR)).hasSize(1);
            assertThat(logs.eventos(Level.ERROR).get(0).getThrowableProxy().getMessage())
                    .isEqualTo("detalle interno secreto");
            // 4xx: nunca con excepcion, y el 409 en WARN sin el valor duplicado.
            assertThat(logs.eventos().stream().filter(e -> e.getLevel() != Level.ERROR))
                    .allSatisfy(e -> assertThat(e.getThrowableProxy()).isNull());
            assertThat(logs.eventos(Level.WARN)).hasSize(1);
            assertThat(logs.texto()).doesNotContain("Duplicate entry").doesNotContain("uk_secreto");
        }
    }

    @Test
    void elLogDeUnCuatrocientosNoIncluyeLaQueryString() throws Exception {
        try (CapturaLogs logs = CapturaLogs.de(GlobalExceptionHandler.class)) {
            mockMvc.perform(get("/prueba/suelto").param("secreto", "valor-privado"))
                    .andExpect(status().isBadRequest());

            assertThat(logs.texto()).contains("/prueba/suelto").doesNotContain("valor-privado");
        }
    }
}
