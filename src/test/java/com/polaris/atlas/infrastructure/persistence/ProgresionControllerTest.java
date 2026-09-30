package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.GetProgresionInterface;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionFilterMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionSesionDtoMapperImpl;
import com.polaris.shared.error.ValidationException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad: se prueba el binding, los codigos de estado y la
 * forma del JSON. El usuario es siempre el actual (del JWT).
 */
@WebMvcTest(controllers = ProgresionController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ProgresionFilterMapperImpl.class, ProgresionSesionDtoMapperImpl.class})
class ProgresionControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetProgresionInterface getProgresion;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static ProgresionSesion punto(Long id, LocalDate fecha, String volumen) {
        return ProgresionSesion.builder().sesionId(id).fecha(fecha).volumen(new BigDecimal(volumen))
                .numeroSeries(3).pesoMaximo(new BigDecimal("82.50")).repsTotales(24).build();
    }

    @Test
    @DisplayName("GET: 200 con un punto por sesion (volumen, series, peso maximo, reps) y el filtro llega al caso de uso con el usuario actual")
    void progresion() throws Exception {
        when(getProgresion.get(eq(USUARIO), any(ProgresionFilter.class))).thenReturn(List.of(
                punto(10L, LocalDate.of(2026, 9, 2), "1980.00"),
                punto(11L, LocalDate.of(2026, 9, 9), "2062.50")));

        mockMvc.perform(get("/api/atlas/progresion")
                        .param("ejercicioId", "5").param("desde", "2026-09-01").param("hasta", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sesionId").value(10))
                .andExpect(jsonPath("$[0].fecha").value("2026-09-02"))
                .andExpect(jsonPath("$[0].volumen").value(1980.00))
                .andExpect(jsonPath("$[0].numeroSeries").value(3))
                .andExpect(jsonPath("$[0].pesoMaximo").value(82.50))
                .andExpect(jsonPath("$[0].repsTotales").value(24))
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$[1].sesionId").value(11))
                .andExpect(jsonPath("$[1].volumen").value(2062.50));

        ArgumentCaptor<ProgresionFilter> filtro = ArgumentCaptor.forClass(ProgresionFilter.class);
        verify(getProgresion).get(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getEjercicioId()).isEqualTo(5L);
        assertThat(filtro.getValue().getDesde()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(filtro.getValue().getHasta()).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    @Test
    @DisplayName("GET: los importes salen con dos decimales en el JSON")
    void escalaEnElJson() throws Exception {
        when(getProgresion.get(eq(USUARIO), any(ProgresionFilter.class))).thenReturn(List.of(
                punto(10L, LocalDate.of(2026, 9, 2), "1980.00")));

        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"volumen\":1980.00")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"pesoMaximo\":82.50")));
    }

    @Test
    @DisplayName("GET: sin datos, 200 con lista vacia; y las fechas son opcionales")
    void sinDatos() throws Exception {
        when(getProgresion.get(eq(USUARIO), any(ProgresionFilter.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", "5"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        ArgumentCaptor<ProgresionFilter> filtro = ArgumentCaptor.forClass(ProgresionFilter.class);
        verify(getProgresion).get(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isNull();
        assertThat(filtro.getValue().getHasta()).isNull();
    }

    @Test
    @DisplayName("GET: 400 legible sin ejercicioId")
    void faltaEjercicio() throws Exception {
        mockMvc.perform(get("/api/atlas/progresion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ejercicioId: es obligatorio"));

        verify(getProgresion, never()).get(any(), any());
    }

    @Test
    @DisplayName("GET: 400 con un ejercicioId o unas fechas ilegibles")
    void filtrosIlegibles() throws Exception {
        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", "abc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", "5").param("desde", "ayer"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", "5").param("hasta", "2026-13-45"))
                .andExpect(status().isBadRequest());

        verify(getProgresion, never()).get(any(), any());
    }

    @Test
    @DisplayName("GET: un desde posterior a hasta (400 del servicio) llega con su mensaje")
    void rangoInvertido() throws Exception {
        when(getProgresion.get(eq(USUARIO), any(ProgresionFilter.class)))
                .thenThrow(new ValidationException("La fecha 'desde' no puede ser posterior a 'hasta'"));

        mockMvc.perform(get("/api/atlas/progresion")
                        .param("ejercicioId", "5").param("desde", "2026-09-30").param("hasta", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La fecha 'desde' no puede ser posterior a 'hasta'"));
    }

    @Test
    @DisplayName("GET: 404 si el ejercicio no existe o no es visible para el usuario")
    void ejercicioNoVisible() throws Exception {
        when(getProgresion.get(eq(USUARIO), any(ProgresionFilter.class)))
                .thenThrow(new EjercicioNotFoundException(99L));

        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", "99"))
                .andExpect(status().isNotFound());
    }
}
