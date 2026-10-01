package com.polaris.shared.web;

import com.polaris.auth.infrastructure.security.JwtService;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La app completa (cadena de filtros real, Spring Security incluido): el
 * X-Request-Id sale tambien en las respuestas que genera Spring Security sin
 * llegar a ningun controller (401), lo que prueba que RequestIdFilter corre
 * ANTES que la cadena de seguridad. Si el orden fuera otro, el 401 saldria sin
 * cabecera.
 */
class RequestIdIntegracionTest extends IntegracionBase {

    private static final String RUTA_PROTEGIDA = "/api/kuiper/categoria";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("un 401 de Spring Security (sin token) lleva X-Request-Id generado")
    void el401LlevaId() throws Exception {
        MvcResult resultado = mockMvc.perform(get(RUTA_PROTEGIDA))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(RequestIdFilter.CABECERA))
                .andReturn();

        assertThat(UUID.fromString(resultado.getResponse().getHeader(RequestIdFilter.CABECERA))).isNotNull();
    }

    @Test
    @DisplayName("un 401 con token invalido respeta el X-Request-Id valido del cliente")
    void el401RespetaElIdDelCliente() throws Exception {
        mockMvc.perform(get(RUTA_PROTEGIDA)
                        .header("Authorization", "Bearer basura")
                        .header(RequestIdFilter.CABECERA, "front-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(RequestIdFilter.CABECERA, "front-401"));
    }

    @Test
    @DisplayName("un id con caracteres fuera de [A-Za-z0-9_-] se descarta tambien en la app completa")
    void elIdMaliciosoSeDescarta() throws Exception {
        String malicioso = "x 2026-10-01 ERROR falso: login de admin {}";

        MvcResult resultado = mockMvc.perform(get(RUTA_PROTEGIDA).header(RequestIdFilter.CABECERA, malicioso))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String devuelto = resultado.getResponse().getHeader(RequestIdFilter.CABECERA);
        assertThat(devuelto).isNotEqualTo(malicioso);
        assertThat(UUID.fromString(devuelto)).isNotNull();
    }

    @Test
    @DisplayName("con saltos de linea el firewall de Spring Security ya rechaza la peticion (400) y el id devuelto es un UUID propio")
    void unIdConSaltosDeLineaNuncaSeRepite() throws Exception {
        String malicioso = "x\r\n2026-10-01 ERROR falso";

        MvcResult resultado = mockMvc.perform(get(RUTA_PROTEGIDA).header(RequestIdFilter.CABECERA, malicioso))
                .andExpect(status().isBadRequest())
                .andReturn();

        String devuelto = resultado.getResponse().getHeader(RequestIdFilter.CABECERA);
        assertThat(devuelto).isNotEqualTo(malicioso);
        assertThat(UUID.fromString(devuelto)).isNotNull();
    }

    @Test
    @DisplayName("un 200 autenticado y un 404 del controlador llevan el id; el cuerpo del error no cambia")
    void el200Yel404LlevanId() throws Exception {
        String token = jwtService.generar(9301L);

        mockMvc.perform(get(RUTA_PROTEGIDA)
                        .header("Authorization", "Bearer " + token)
                        .header(RequestIdFilter.CABECERA, "ok-200"))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIdFilter.CABECERA, "ok-200"));

        mockMvc.perform(get("/api/no-existe")
                        .header("Authorization", "Bearer " + token)
                        .header(RequestIdFilter.CABECERA, "nf-404"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(RequestIdFilter.CABECERA, "nf-404"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.requestId").doesNotExist());
    }

    @Test
    @DisplayName("una ruta publica (/health) tambien lleva el id")
    void laRutaPublicaLlevaId() throws Exception {
        mockMvc.perform(get("/health").header(RequestIdFilter.CABECERA, "salud-1"))
                .andExpect(header().string(RequestIdFilter.CABECERA, "salud-1"));
    }
}
