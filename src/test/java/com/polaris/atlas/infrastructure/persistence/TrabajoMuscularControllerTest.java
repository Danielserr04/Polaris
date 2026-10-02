package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.GetTrabajoMuscularInterface;
import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.domain.model.TrabajoMuscularFilter;
import com.polaris.atlas.infrastructure.persistence.mapper.TrabajoMuscularDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.TrabajoMuscularFilterMapperImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad: binding, codigos de estado y forma del JSON. El
 * usuario es siempre el actual (del JWT).
 */
@WebMvcTest(controllers = TrabajoMuscularController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({TrabajoMuscularFilterMapperImpl.class, TrabajoMuscularDtoMapperImpl.class})
class TrabajoMuscularControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTrabajoMuscularInterface getTrabajoMuscular;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    @Test
    @DisplayName("GET: 200 con una fila por grupo y el rango llega al caso de uso con el usuario actual")
    void trabajo() throws Exception {
        when(getTrabajoMuscular.get(eq(USUARIO), any(TrabajoMuscularFilter.class))).thenReturn(List.of(
                TrabajoMuscular.builder().grupoMuscular("Pecho").numeroSeries(12).numeroSesiones(3)
                        .volumen(new BigDecimal("4800.00")).build()));

        mockMvc.perform(get("/api/atlas/trabajo-muscular").param("desde", "2026-09-01").param("hasta", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grupoMuscular").value("Pecho"))
                .andExpect(jsonPath("$[0].numeroSeries").value(12))
                .andExpect(jsonPath("$[0].numeroSesiones").value(3))
                .andExpect(jsonPath("$[0].volumen").value(4800.00));

        ArgumentCaptor<TrabajoMuscularFilter> filtro = ArgumentCaptor.forClass(TrabajoMuscularFilter.class);
        verify(getTrabajoMuscular).get(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(filtro.getValue().getHasta()).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    @Test
    @DisplayName("GET: 400 si una fecha no tiene formato yyyy-MM-dd o el rango esta al reves")
    void errores() throws Exception {
        mockMvc.perform(get("/api/atlas/trabajo-muscular").param("desde", "ayer"))
                .andExpect(status().isBadRequest());

        when(getTrabajoMuscular.get(eq(USUARIO), any(TrabajoMuscularFilter.class)))
                .thenThrow(new ValidationException("La fecha 'desde' no puede ser posterior a 'hasta'"));
        mockMvc.perform(get("/api/atlas/trabajo-muscular").param("desde", "2026-09-30").param("hasta", "2026-09-01"))
                .andExpect(status().isBadRequest());
    }
}
