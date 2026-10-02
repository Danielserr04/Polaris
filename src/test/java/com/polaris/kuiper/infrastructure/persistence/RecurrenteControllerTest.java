package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateRecurrenteInterface;
import com.polaris.kuiper.application.in.DeleteRecurrenteInterface;
import com.polaris.kuiper.application.in.GetRecurrenteInterface;
import com.polaris.kuiper.application.in.ListRecurrenteInterface;
import com.polaris.kuiper.application.in.UpdateRecurrenteInterface;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;
import com.polaris.kuiper.domain.model.RecurrenteNotFoundException;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteFilterMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteFormDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteListDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteRequestDtoMapperImpl;
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
 * forma del JSON. Que el usuario sea siempre el actual, que nunca salga el
 * usuarioId y que proximaFecha y cuotasPagadas del body se ignoren son parte
 * del contrato.
 */
@WebMvcTest(controllers = RecurrenteController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({RecurrenteRequestDtoMapperImpl.class, RecurrenteFilterMapperImpl.class,
        RecurrenteFormDtoMapperImpl.class, RecurrenteListDtoMapperImpl.class})
class RecurrenteControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateRecurrenteInterface createRecurrente;

    @MockitoBean
    private GetRecurrenteInterface getRecurrente;

    @MockitoBean
    private ListRecurrenteInterface listRecurrente;

    @MockitoBean
    private UpdateRecurrenteInterface updateRecurrente;

    @MockitoBean
    private DeleteRecurrenteInterface deleteRecurrente;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static Recurrente netflix() {
        Categoria ocio = Categoria.builder().id(3L).usuarioId(USUARIO).nombre("Ocio").color("#ff0000")
                .icono("tv").tipo(TipoMovimiento.GASTO).build();
        return Recurrente.builder().id(10L).usuarioId(USUARIO).concepto("Netflix")
                .importe(new BigDecimal("12.99")).tipo(TipoMovimiento.GASTO).categoriaId(3L).categoria(ocio)
                .metodoPago("Tarjeta").frecuencia(FrecuenciaRecurrente.MENSUAL)
                .fechaInicio(LocalDate.of(2026, 1, 5)).proximaFecha(LocalDate.of(2026, 10, 5))
                .cuotasTotal(12).cuotasPagadas(9).activo(true).build();
    }

    private static final String CUERPO_VALIDO = "{\"concepto\":\"Netflix\",\"importe\":12.99,\"tipo\":\"GASTO\","
            + "\"categoriaId\":3,\"metodoPago\":\"Tarjeta\",\"frecuencia\":\"MENSUAL\","
            + "\"fechaInicio\":\"2026-01-05\",\"cuotasTotal\":12}";

    @Test
    @DisplayName("GET lista: 200, con la categoria aplanada y sin usuarioId ni metodo de pago, y el filtro llega al caso de uso")
    void listaConFiltros() throws Exception {
        when(listRecurrente.list(eq(USUARIO), any(RecurrenteFilter.class))).thenReturn(List.of(netflix()));

        mockMvc.perform(get("/api/kuiper/recurrente").param("activo", "true").param("tipo", "GASTO")
                        .param("categoriaId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].importe").value(12.99))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Ocio"))
                .andExpect(jsonPath("$[0].categoriaColor").value("#ff0000"))
                .andExpect(jsonPath("$[0].frecuencia").value("MENSUAL"))
                .andExpect(jsonPath("$[0].proximaFecha").value("2026-10-05"))
                .andExpect(jsonPath("$[0].cuotasTotal").value(12))
                .andExpect(jsonPath("$[0].cuotasPagadas").value(9))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].metodoPago").doesNotExist())
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist());

        ArgumentCaptor<RecurrenteFilter> filtro = ArgumentCaptor.forClass(RecurrenteFilter.class);
        verify(listRecurrente).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getActivo()).isTrue();
        assertThat(filtro.getValue().getTipo()).isEqualTo(TipoMovimiento.GASTO);
        assertThat(filtro.getValue().getCategoriaId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("GET lista: 400 si un filtro no tiene formato valido")
    void listaFiltroInvalido() throws Exception {
        mockMvc.perform(get("/api/kuiper/recurrente").param("tipo", "REGALO"))
                .andExpect(status().isBadRequest());

        verify(listRecurrente, never()).list(any(), any());
    }

    @Test
    @DisplayName("GET detalle: 200 con la ficha completa, y siempre para el usuario actual")
    void detalle() throws Exception {
        when(getRecurrente.get(USUARIO, 10L)).thenReturn(netflix());

        mockMvc.perform(get("/api/kuiper/recurrente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.metodoPago").value("Tarjeta"))
                .andExpect(jsonPath("$.fechaInicio").value("2026-01-05"))
                .andExpect(jsonPath("$.categoriaIcono").value("tv"))
                .andExpect(jsonPath("$.usuarioId").doesNotExist());
    }

    @Test
    @DisplayName("GET detalle: 404 si no existe o es de otro usuario")
    void detalleNoEncontrado() throws Exception {
        when(getRecurrente.get(USUARIO, 99L)).thenThrow(new RecurrenteNotFoundException(99L));

        mockMvc.perform(get("/api/kuiper/recurrente/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST: 201, el usuario es el actual y usuarioId, proximaFecha y cuotasPagadas del body se ignoran")
    void crea() throws Exception {
        when(createRecurrente.create(eq(USUARIO), any(Recurrente.class))).thenReturn(netflix());

        mockMvc.perform(post("/api/kuiper/recurrente").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"concepto\":\"Netflix\",\"importe\":12.99,\"tipo\":\"GASTO\",\"categoriaId\":3,"
                                + "\"frecuencia\":\"MENSUAL\",\"fechaInicio\":\"2026-01-05\",\"usuarioId\":999,"
                                + "\"proximaFecha\":\"2030-01-01\",\"cuotasPagadas\":50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<Recurrente> enviado = ArgumentCaptor.forClass(Recurrente.class);
        verify(createRecurrente).create(eq(USUARIO), enviado.capture());
        assertThat(enviado.getValue().getId()).isNull();
        assertThat(enviado.getValue().getUsuarioId()).isNull();
        assertThat(enviado.getValue().getProximaFecha()).isNull();
        assertThat(enviado.getValue().getCuotasPagadas()).isZero();
        assertThat(enviado.getValue().getCuotasTotal()).isNull();
        assertThat(enviado.getValue().getImporte()).isEqualByComparingTo("12.99");
        assertThat(enviado.getValue().isActivo()).as("sin activo en el body, true").isTrue();
    }

    @Test
    @DisplayName("POST: activo=false llega como pausado")
    void creaPausado() throws Exception {
        when(createRecurrente.create(eq(USUARIO), any(Recurrente.class))).thenReturn(netflix());

        mockMvc.perform(post("/api/kuiper/recurrente").contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO.replace("}", ",\"activo\":false}")))
                .andExpect(status().isCreated());

        ArgumentCaptor<Recurrente> enviado = ArgumentCaptor.forClass(Recurrente.class);
        verify(createRecurrente).create(eq(USUARIO), enviado.capture());
        assertThat(enviado.getValue().isActivo()).isFalse();
    }

    @Test
    @DisplayName("POST: 400 con campos obligatorios ausentes, importe no positivo o con 3 decimales, o cuotas fuera de rango")
    void creaInvalido() throws Exception {
        String[] cuerpos = {
                CUERPO_VALIDO.replace("\"concepto\":\"Netflix\",", ""),
                CUERPO_VALIDO.replace("\"Netflix\"", "\" \""),
                CUERPO_VALIDO.replace("\"Netflix\"", "\"" + "x".repeat(201) + "\""),
                CUERPO_VALIDO.replace("12.99", "0"),
                CUERPO_VALIDO.replace("12.99", "-5"),
                CUERPO_VALIDO.replace("12.99", "1.999"),
                CUERPO_VALIDO.replace("\"tipo\":\"GASTO\",", ""),
                CUERPO_VALIDO.replace("\"categoriaId\":3,", ""),
                CUERPO_VALIDO.replace("\"MENSUAL\"", "\"DIARIA\""),
                CUERPO_VALIDO.replace(",\"fechaInicio\":\"2026-01-05\"", ""),
                CUERPO_VALIDO.replace("\"cuotasTotal\":12", "\"cuotasTotal\":0"),
                CUERPO_VALIDO.replace("\"cuotasTotal\":12", "\"cuotasTotal\":601"),
                CUERPO_VALIDO.replace("\"Tarjeta\"", "\"" + "t".repeat(51) + "\"")
        };

        for (String cuerpo : cuerpos) {
            mockMvc.perform(post("/api/kuiper/recurrente").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }

        verify(createRecurrente, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST: 400 si el tipo no coincide con la categoria y 404 si la categoria es de otro usuario")
    void creaRechazado() throws Exception {
        when(createRecurrente.create(eq(USUARIO), any(Recurrente.class)))
                .thenThrow(new ValidationException("El tipo del recurrente no coincide con el de su categoria"))
                .thenThrow(new CategoriaNotFoundException(3L));

        mockMvc.perform(post("/api/kuiper/recurrente").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El tipo del recurrente no coincide con el de su categoria"));
        mockMvc.perform(post("/api/kuiper/recurrente").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT: 200 con la ficha actualizada, para el usuario actual, y activo=false llega como pausa")
    void actualiza() throws Exception {
        when(updateRecurrente.update(eq(USUARIO), eq(10L), any(Recurrente.class))).thenReturn(netflix());

        mockMvc.perform(put("/api/kuiper/recurrente/10").contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO.replace("}", ",\"activo\":false}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepto").value("Netflix"));

        ArgumentCaptor<Recurrente> enviado = ArgumentCaptor.forClass(Recurrente.class);
        verify(updateRecurrente).update(eq(USUARIO), eq(10L), enviado.capture());
        assertThat(enviado.getValue().isActivo()).isFalse();
        assertThat(enviado.getValue().getCuotasTotal()).isEqualTo(12);
    }

    @Test
    @DisplayName("PUT: 404 si es de otro usuario y 400 con el cuerpo invalido, sin llegar al caso de uso")
    void actualizaRechazado() throws Exception {
        when(updateRecurrente.update(eq(USUARIO), eq(12L), any(Recurrente.class)))
                .thenThrow(new RecurrenteNotFoundException(12L));

        mockMvc.perform(put("/api/kuiper/recurrente/12").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/kuiper/recurrente/10").contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO.replace("12.99", "0")))
                .andExpect(status().isBadRequest());

        verify(updateRecurrente, never()).update(eq(USUARIO), eq(10L), any());
    }

    @Test
    @DisplayName("DELETE: 204 y siempre para el usuario actual; 404 si es de otro usuario")
    void borra() throws Exception {
        doThrow(new RecurrenteNotFoundException(12L)).when(deleteRecurrente).delete(USUARIO, 12L);

        mockMvc.perform(delete("/api/kuiper/recurrente/10")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/kuiper/recurrente/12")).andExpect(status().isNotFound());

        verify(deleteRecurrente).delete(USUARIO, 10L);
    }
}
