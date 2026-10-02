package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.AportarMetaAhorroInterface;
import com.polaris.kuiper.application.in.CreateMetaAhorroInterface;
import com.polaris.kuiper.application.in.DeleteAportacionMetaInterface;
import com.polaris.kuiper.application.in.DeleteMetaAhorroInterface;
import com.polaris.kuiper.application.in.GetMetaAhorroInterface;
import com.polaris.kuiper.application.in.ListAportacionMetaInterface;
import com.polaris.kuiper.application.in.ListMetaAhorroInterface;
import com.polaris.kuiper.application.in.UpdateMetaAhorroInterface;
import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.domain.model.MetaAhorroFilter;
import com.polaris.kuiper.domain.model.MetaAhorroNotFoundException;
import com.polaris.kuiper.infrastructure.persistence.mapper.AportacionMetaDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.AportacionMetaRequestDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroFilterMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroFormDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroListDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroRequestDtoMapperImpl;
import com.polaris.shared.error.DuplicateResourceException;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad: se prueba el binding, los codigos de estado y la
 * forma del JSON (con los calculados), no quien puede llamar. Que el usuario
 * sea siempre el actual y que nunca salga el usuarioId son parte del contrato.
 */
@WebMvcTest(controllers = MetaAhorroController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({MetaAhorroRequestDtoMapperImpl.class, MetaAhorroFilterMapperImpl.class, MetaAhorroFormDtoMapperImpl.class,
        MetaAhorroListDtoMapperImpl.class, AportacionMetaRequestDtoMapperImpl.class, AportacionMetaDtoMapperImpl.class})
class MetaAhorroControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateMetaAhorroInterface createMeta;
    @MockitoBean
    private GetMetaAhorroInterface getMeta;
    @MockitoBean
    private ListMetaAhorroInterface listMeta;
    @MockitoBean
    private UpdateMetaAhorroInterface updateMeta;
    @MockitoBean
    private DeleteMetaAhorroInterface deleteMeta;
    @MockitoBean
    private AportarMetaAhorroInterface aportarMeta;
    @MockitoBean
    private ListAportacionMetaInterface listAportacion;
    @MockitoBean
    private DeleteAportacionMetaInterface deleteAportacion;
    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static final String CUERPO_VALIDO =
            "{\"nombre\":\"Viaje\",\"importeObjetivo\":3000.00,\"fechaLimite\":\"2027-06-30\",\"color\":\"#5bb3a0\",\"icono\":\"plane\"}";

    private static MetaAhorro meta() {
        MetaAhorro m = MetaAhorro.builder().id(10L).usuarioId(USUARIO).nombre("Viaje")
                .importeObjetivo(new BigDecimal("3000.00")).fechaLimite(LocalDate.of(2026, 12, 31))
                .color("#5bb3a0").icono("plane").creadaEn(Instant.parse("2026-09-01T08:00:00Z"))
                .importeActual(new BigDecimal("750.00")).build();
        m.calcularPlazo(LocalDate.of(2026, 10, 2));
        return m;
    }

    @Test
    @DisplayName("GET lista: 200 con los calculados, sin usuarioId ni creadaEn, y el filtro llega al caso de uso")
    void lista() throws Exception {
        when(listMeta.list(eq(USUARIO), any(MetaAhorroFilter.class))).thenReturn(List.of(meta()));

        mockMvc.perform(get("/api/kuiper/meta").param("completada", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nombre").value("Viaje"))
                .andExpect(jsonPath("$[0].importeObjetivo").value(3000.00))
                .andExpect(jsonPath("$[0].importeActual").value(750.00))
                .andExpect(jsonPath("$[0].porcentaje").value(25.0))
                .andExpect(jsonPath("$[0].restante").value(2250.00))
                .andExpect(jsonPath("$[0].completada").value(false))
                .andExpect(jsonPath("$[0].diasRestantes").value(90))
                .andExpect(jsonPath("$[0].ahorroMensualNecesario").value(750.00))
                .andExpect(jsonPath("$[0].fechaLimite").value("2026-12-31"))
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$[0].creadaEn").doesNotExist());

        ArgumentCaptor<MetaAhorroFilter> filtro = ArgumentCaptor.forClass(MetaAhorroFilter.class);
        verify(listMeta).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getCompletada()).isFalse();
    }

    @Test
    @DisplayName("GET detalle: 200 con creadaEn; 404 si no es tuya")
    void detalle() throws Exception {
        when(getMeta.get(USUARIO, 10L)).thenReturn(meta());
        when(getMeta.get(USUARIO, 99L)).thenThrow(new MetaAhorroNotFoundException(99L));

        mockMvc.perform(get("/api/kuiper/meta/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creadaEn").exists())
                .andExpect(jsonPath("$.color").value("#5bb3a0"))
                .andExpect(jsonPath("$.usuarioId").doesNotExist());
        mockMvc.perform(get("/api/kuiper/meta/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST: 201 y el cuerpo llega mapeado al caso de uso")
    void crear() throws Exception {
        when(createMeta.create(eq(USUARIO), any(MetaAhorro.class))).thenReturn(meta());

        mockMvc.perform(post("/api/kuiper/meta").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<MetaAhorro> enviada = ArgumentCaptor.forClass(MetaAhorro.class);
        verify(createMeta).create(eq(USUARIO), enviada.capture());
        assertThat(enviada.getValue().getNombre()).isEqualTo("Viaje");
        assertThat(enviada.getValue().getImporteObjetivo()).isEqualByComparingTo("3000.00");
        assertThat(enviada.getValue().getFechaLimite()).isEqualTo(LocalDate.of(2027, 6, 30));
        assertThat(enviada.getValue().getUsuarioId()).isNull();
    }

    @Test
    @DisplayName("POST invalido: objetivo 0 o negativo, sin nombre, color mal formado o 3 decimales dan 400")
    void crearInvalido() throws Exception {
        for (String cuerpo : List.of(
                "{\"nombre\":\"Viaje\",\"importeObjetivo\":0}",
                "{\"nombre\":\"Viaje\",\"importeObjetivo\":-5}",
                "{\"nombre\":\"\",\"importeObjetivo\":100}",
                "{\"nombre\":\"Viaje\"}",
                "{\"nombre\":\"Viaje\",\"importeObjetivo\":100,\"color\":\"rojo\"}",
                "{\"nombre\":\"Viaje\",\"importeObjetivo\":100.001}")) {
            mockMvc.perform(post("/api/kuiper/meta").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }
        verify(createMeta, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST con nombre repetido: 409; PUT: 200 y 404")
    void conflictoYEdicion() throws Exception {
        when(createMeta.create(eq(USUARIO), any(MetaAhorro.class)))
                .thenThrow(new DuplicateResourceException("Ya tienes una meta con ese nombre"));
        when(updateMeta.update(eq(USUARIO), eq(10L), any(MetaAhorro.class))).thenReturn(meta());
        when(updateMeta.update(eq(USUARIO), eq(11L), any(MetaAhorro.class)))
                .thenThrow(new MetaAhorroNotFoundException(11L));

        mockMvc.perform(post("/api/kuiper/meta").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/kuiper/meta/10").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Viaje"));
        mockMvc.perform(put("/api/kuiper/meta/11").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE: 204 con el usuario actual")
    void borrar() throws Exception {
        mockMvc.perform(delete("/api/kuiper/meta/10"))
                .andExpect(status().isNoContent());

        verify(deleteMeta).delete(USUARIO, 10L);
    }

    @Test
    @DisplayName("POST aportacion: 201 con la meta actualizada; importe con signo, fecha y nota opcionales")
    void aportar() throws Exception {
        when(aportarMeta.aportar(eq(USUARIO), eq(10L), any(AportacionMeta.class))).thenReturn(meta());

        mockMvc.perform(post("/api/kuiper/meta/10/aportacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"importe\":-50.25,\"nota\":\"Imprevisto\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.importeActual").value(750.00));

        ArgumentCaptor<AportacionMeta> enviada = ArgumentCaptor.forClass(AportacionMeta.class);
        verify(aportarMeta).aportar(eq(USUARIO), eq(10L), enviada.capture());
        assertThat(enviada.getValue().getImporte()).isEqualByComparingTo("-50.25");
        assertThat(enviada.getValue().getFecha()).isNull();
        assertThat(enviada.getValue().getNota()).isEqualTo("Imprevisto");
        assertThat(enviada.getValue().getMetaId()).isNull();
    }

    @Test
    @DisplayName("POST aportacion: sin importe 400 por formato; retirar de mas 400 con el mensaje del dominio")
    void aportarInvalido() throws Exception {
        when(aportarMeta.aportar(eq(USUARIO), eq(10L), any(AportacionMeta.class)))
                .thenThrow(new ValidationException("No se puede retirar mas de lo ahorrado en la meta"));

        mockMvc.perform(post("/api/kuiper/meta/10/aportacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":\"x\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/kuiper/meta/10/aportacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"importe\":-9999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede retirar mas de lo ahorrado en la meta"));
    }

    @Test
    @DisplayName("GET historial: 200 sin usuarioId ni metaId; DELETE aportacion: 204 y 400")
    void historialYBorrado() throws Exception {
        when(listAportacion.listAportaciones(USUARIO, 10L)).thenReturn(List.of(
                AportacionMeta.builder().id(2L).usuarioId(USUARIO).metaId(10L).fecha(LocalDate.of(2026, 9, 30))
                        .importe(new BigDecimal("-20.00")).build(),
                AportacionMeta.builder().id(1L).usuarioId(USUARIO).metaId(10L).fecha(LocalDate.of(2026, 9, 1))
                        .importe(new BigDecimal("100.00")).nota("Nomina").build()));
        doThrow(new ValidationException("No se puede borrar: el total de la meta quedaria en negativo"))
                .when(deleteAportacion).deleteAportacion(USUARIO, 10L, 1L);

        mockMvc.perform(get("/api/kuiper/meta/10/aportacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].importe").value(-20.00))
                .andExpect(jsonPath("$[1].nota").value("Nomina"))
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$[0].metaId").doesNotExist());
        mockMvc.perform(delete("/api/kuiper/meta/10/aportacion/2"))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/kuiper/meta/10/aportacion/1"))
                .andExpect(status().isBadRequest());

        verify(deleteAportacion).deleteAportacion(USUARIO, 10L, 2L);
    }
}
