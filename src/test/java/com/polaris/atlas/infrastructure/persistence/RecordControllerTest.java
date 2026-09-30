package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.ListRecordsInterface;
import com.polaris.atlas.domain.model.RecordEjercicio;
import com.polaris.atlas.infrastructure.persistence.mapper.RecordEjercicioDtoMapperImpl;
import com.polaris.shared.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sin filtros de seguridad: binding, codigos de estado y forma del JSON. */
@WebMvcTest(controllers = RecordController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import(RecordEjercicioDtoMapperImpl.class)
class RecordControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListRecordsInterface listRecords;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    @Test
    @DisplayName("GET: 200 con una marca por ejercicio (peso maximo con reps y fecha, mayor volumen con fecha) para el usuario actual")
    void records() throws Exception {
        when(listRecords.list(USUARIO)).thenReturn(List.of(
                RecordEjercicio.builder().ejercicioId(2L).ejercicioNombre("Press banca")
                        .ejercicioGrupoMuscular("Pecho").pesoMaximo(new BigDecimal("100.00")).repsPesoMaximo(5)
                        .fechaPesoMaximo(LocalDate.of(2026, 9, 9)).volumenMaximoSesion(new BigDecimal("3100.50"))
                        .fechaVolumenMaximo(LocalDate.of(2026, 9, 16)).build()));

        mockMvc.perform(get("/api/atlas/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ejercicioId").value(2))
                .andExpect(jsonPath("$[0].ejercicioNombre").value("Press banca"))
                .andExpect(jsonPath("$[0].ejercicioGrupoMuscular").value("Pecho"))
                .andExpect(jsonPath("$[0].pesoMaximo").value(100.00))
                .andExpect(jsonPath("$[0].repsPesoMaximo").value(5))
                .andExpect(jsonPath("$[0].fechaPesoMaximo").value("2026-09-09"))
                .andExpect(jsonPath("$[0].volumenMaximoSesion").value(3100.50))
                .andExpect(jsonPath("$[0].fechaVolumenMaximo").value("2026-09-16"))
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"pesoMaximo\":100.00")));

        verify(listRecords).list(USUARIO);
    }

    @Test
    @DisplayName("GET: un ejercicio solo con peso corporal sale sin los campos de volumen")
    void sinVolumen() throws Exception {
        when(listRecords.list(USUARIO)).thenReturn(List.of(
                RecordEjercicio.builder().ejercicioId(3L).ejercicioNombre("Dominadas")
                        .ejercicioGrupoMuscular("Espalda").pesoMaximo(new BigDecimal("0.00")).repsPesoMaximo(12)
                        .fechaPesoMaximo(LocalDate.of(2026, 9, 9)).build()));

        mockMvc.perform(get("/api/atlas/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pesoMaximo").value(0.00))
                .andExpect(jsonPath("$[0].repsPesoMaximo").value(12))
                .andExpect(jsonPath("$[0].volumenMaximoSesion").doesNotExist())
                .andExpect(jsonPath("$[0].fechaVolumenMaximo").doesNotExist());
    }

    @Test
    @DisplayName("GET: sin series, 200 con lista vacia")
    void sinDatos() throws Exception {
        when(listRecords.list(USUARIO)).thenReturn(List.of());

        mockMvc.perform(get("/api/atlas/records"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("POST no esta soportado: solo lectura")
    void soloLectura() throws Exception {
        mockMvc.perform(post("/api/atlas/records")).andExpect(status().isMethodNotAllowed());
    }
}
