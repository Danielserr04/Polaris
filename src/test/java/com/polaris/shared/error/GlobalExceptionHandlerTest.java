package com.polaris.shared.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
