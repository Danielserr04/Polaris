package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.GetResumenDiarioInterface;
import com.polaris.fusion.domain.model.MacroResumen;
import com.polaris.fusion.domain.model.ResumenDiario;
import com.polaris.fusion.infrastructure.persistence.mapper.ResumenDiarioDtoMapperImpl;
import com.polaris.shared.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad: se prueba el binding de {@code ?fecha=}, el 400 y
 * la forma del JSON, no quien puede llamar (eso lo decide SecurityConfig).
 */
@WebMvcTest(controllers = ResumenDiarioController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import(ResumenDiarioDtoMapperImpl.class)
class ResumenDiarioControllerTest {

    private static final Long USUARIO = 7L;
    private static final LocalDate DIA = LocalDate.of(2026, 9, 30);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetResumenDiarioInterface getResumenDiario;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static MacroResumen macro(String consumido, String objetivo, String restante, String porcentaje) {
        return MacroResumen.builder().consumido(new BigDecimal(consumido))
                .objetivo(objetivo == null ? null : new BigDecimal(objetivo))
                .restante(restante == null ? null : new BigDecimal(restante))
                .porcentaje(porcentaje == null ? null : new BigDecimal(porcentaje)).build();
    }

    private static ResumenDiario resumen(LocalDate fecha, MacroResumen m, LocalDate desde) {
        return ResumenDiario.builder().fecha(fecha).objetivoVigenteDesde(desde)
                .kcal(m).proteinas(m).carbohidratos(m).grasas(m).build();
    }

    @Test
    @DisplayName("200 con la fecha pedida: fecha, macros y objetivo en el JSON, importes como numeros")
    void devuelve200ConObjetivo() throws Exception {
        when(getResumenDiario.get(USUARIO, DIA)).thenReturn(
                resumen(DIA, macro("672.50", "2000.00", "1327.50", "33.63"), LocalDate.of(2026, 9, 1)));

        mockMvc.perform(get("/api/fusion/resumen").param("fecha", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-09-30"))
                .andExpect(jsonPath("$.objetivoVigenteDesde").value("2026-09-01"))
                .andExpect(jsonPath("$.kcal.consumido").value(672.5))
                .andExpect(jsonPath("$.kcal.objetivo").value(2000.0))
                .andExpect(jsonPath("$.kcal.restante").value(1327.5))
                .andExpect(jsonPath("$.kcal.porcentaje").value(33.63))
                .andExpect(jsonPath("$.proteinas.consumido").value(672.5))
                .andExpect(jsonPath("$.carbohidratos.restante").value(1327.5))
                .andExpect(jsonPath("$.grasas.porcentaje").value(33.63));
    }

    @Test
    @DisplayName("200 sin objetivo: objetivo, restante y porcentaje no salen (null se omite, non_null global)")
    void devuelve200SinObjetivo() throws Exception {
        when(getResumenDiario.get(USUARIO, DIA)).thenReturn(
                resumen(DIA, macro("260.00", null, null, null), null));

        mockMvc.perform(get("/api/fusion/resumen").param("fecha", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivoVigenteDesde").doesNotExist())
                .andExpect(jsonPath("$.kcal.consumido").value(260.0))
                .andExpect(jsonPath("$.kcal.objetivo").doesNotExist())
                .andExpect(jsonPath("$.kcal.restante").doesNotExist())
                .andExpect(jsonPath("$.kcal.porcentaje").doesNotExist());
    }

    @Test
    @DisplayName("sin fecha usa hoy y siempre el usuario autenticado")
    void sinFechaUsaHoy() throws Exception {
        when(getResumenDiario.get(eq(USUARIO), any(LocalDate.class))).thenReturn(
                resumen(DIA, macro("0.00", null, null, null), null));

        LocalDate antes = LocalDate.now();
        mockMvc.perform(get("/api/fusion/resumen")).andExpect(status().isOk());
        LocalDate despues = LocalDate.now();

        ArgumentCaptor<LocalDate> fecha = ArgumentCaptor.forClass(LocalDate.class);
        verify(getResumenDiario).get(eq(USUARIO), fecha.capture());
        assertThat(fecha.getValue()).isBetween(antes, despues);
    }

    @Test
    @DisplayName("400 con una fecha invalida, y no llega al servicio")
    void fechaInvalidaDa400() throws Exception {
        mockMvc.perform(get("/api/fusion/resumen").param("fecha", "30-09-2026"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/fusion/resumen").param("fecha", "2026-02-30"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/fusion/resumen").param("fecha", "ayer"))
                .andExpect(status().isBadRequest());

        verify(getResumenDiario, never()).get(any(), any());
    }
}
