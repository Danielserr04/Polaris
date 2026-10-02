package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.BorrarNotificacionesLeidasInterface;
import com.polaris.kuiper.application.in.ContarNotificacionesNoLeidasInterface;
import com.polaris.kuiper.application.in.DeleteNotificacionInterface;
import com.polaris.kuiper.application.in.GetNotificacionInterface;
import com.polaris.kuiper.application.in.LeerTodasNotificacionesInterface;
import com.polaris.kuiper.application.in.ListNotificacionInterface;
import com.polaris.kuiper.application.in.UpdateNotificacionInterface;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.NotificacionFilter;
import com.polaris.kuiper.domain.model.NotificacionNotFoundException;
import com.polaris.kuiper.domain.model.TipoNotificacion;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionFilterMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionFormDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionListDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionRequestDtoMapperImpl;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
 * usuarioId y que la clave no salga en el listado son parte del contrato.
 */
@WebMvcTest(controllers = NotificacionController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({NotificacionRequestDtoMapperImpl.class, NotificacionFilterMapperImpl.class,
        NotificacionFormDtoMapperImpl.class, NotificacionListDtoMapperImpl.class})
class NotificacionControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListNotificacionInterface listNotificacion;

    @MockitoBean
    private GetNotificacionInterface getNotificacion;

    @MockitoBean
    private ContarNotificacionesNoLeidasInterface contarNoLeidas;

    @MockitoBean
    private UpdateNotificacionInterface updateNotificacion;

    @MockitoBean
    private LeerTodasNotificacionesInterface leerTodas;

    @MockitoBean
    private DeleteNotificacionInterface deleteNotificacion;

    @MockitoBean
    private BorrarNotificacionesLeidasInterface borrarLeidas;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static Notificacion notificacion(boolean leida) {
        return Notificacion.builder().id(3L).usuarioId(USUARIO).tipo(TipoNotificacion.PRESUPUESTO_EXCEDIDO)
                .clave("presupuesto-excedido-7-2026-10").titulo("Presupuesto superado: Comida")
                .texto("Llevas 120,00 de 100,00 en octubre de 2026.").enlace("presupuestos").leida(leida)
                .creadaEn(LocalDateTime.of(2026, 10, 2, 8, 0)).build();
    }

    @Test
    @DisplayName("GET lista: 200, sin usuarioId ni clave, y soloNoLeidas llega al caso de uso")
    void lista() throws Exception {
        when(listNotificacion.list(eq(USUARIO), any(NotificacionFilter.class))).thenReturn(List.of(notificacion(false)));

        mockMvc.perform(get("/api/kuiper/notificacion").param("soloNoLeidas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].tipo").value("PRESUPUESTO_EXCEDIDO"))
                .andExpect(jsonPath("$[0].enlace").value("presupuestos"))
                .andExpect(jsonPath("$[0].leida").value(false))
                .andExpect(jsonPath("$[0].creadaEn").value("2026-10-02T08:00:00"))
                .andExpect(jsonPath("$[0].clave").doesNotExist())
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist());

        ArgumentCaptor<NotificacionFilter> filtro = ArgumentCaptor.forClass(NotificacionFilter.class);
        verify(listNotificacion).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getSoloNoLeidas()).isTrue();
    }

    @Test
    @DisplayName("GET /no-leidas/total: el numero de la campana, no se confunde con /{id}")
    void totalNoLeidas() throws Exception {
        when(contarNoLeidas.contarNoLeidas(USUARIO)).thenReturn(4L);

        mockMvc.perform(get("/api/kuiper/notificacion/no-leidas/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4));
    }

    @Test
    @DisplayName("GET /{id} de otro usuario: 404")
    void getDeOtroEs404() throws Exception {
        when(getNotificacion.get(USUARIO, 3L)).thenThrow(new NotificacionNotFoundException(3L));

        mockMvc.perform(get("/api/kuiper/notificacion/3")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{id}/leida sin cuerpo la marca leida y devuelve la ficha con la clave")
    void marcarLeidaSinCuerpo() throws Exception {
        when(updateNotificacion.update(eq(USUARIO), eq(3L), any(Notificacion.class))).thenReturn(notificacion(true));

        mockMvc.perform(put("/api/kuiper/notificacion/3/leida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true))
                .andExpect(jsonPath("$.clave").value("presupuesto-excedido-7-2026-10"))
                .andExpect(jsonPath("$.usuarioId").doesNotExist());

        ArgumentCaptor<Notificacion> cambios = ArgumentCaptor.forClass(Notificacion.class);
        verify(updateNotificacion).update(eq(USUARIO), eq(3L), cambios.capture());
        assertThat(cambios.getValue().isLeida()).isTrue();
    }

    @Test
    @DisplayName("PUT /{id}/leida con leida=false la vuelve a dejar sin leer")
    void marcarNoLeida() throws Exception {
        when(updateNotificacion.update(eq(USUARIO), eq(3L), any(Notificacion.class))).thenReturn(notificacion(false));

        mockMvc.perform(put("/api/kuiper/notificacion/3/leida")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"leida\":false}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Notificacion> cambios = ArgumentCaptor.forClass(Notificacion.class);
        verify(updateNotificacion).update(eq(USUARIO), eq(3L), cambios.capture());
        assertThat(cambios.getValue().isLeida()).isFalse();
    }

    @Test
    @DisplayName("POST /leer-todas: 204 y para el usuario actual")
    void leerTodas() throws Exception {
        mockMvc.perform(post("/api/kuiper/notificacion/leer-todas")).andExpect(status().isNoContent());

        verify(leerTodas).leerTodas(USUARIO);
    }

    @Test
    @DisplayName("DELETE /{id}: 204, y 404 si no es suya")
    void borrarUna() throws Exception {
        mockMvc.perform(delete("/api/kuiper/notificacion/3")).andExpect(status().isNoContent());
        verify(deleteNotificacion).delete(USUARIO, 3L);

        doThrow(new NotificacionNotFoundException(4L)).when(deleteNotificacion).delete(USUARIO, 4L);
        mockMvc.perform(delete("/api/kuiper/notificacion/4")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE sin id: borra las leidas del usuario actual")
    void borrarLeidas() throws Exception {
        mockMvc.perform(delete("/api/kuiper/notificacion")).andExpect(status().isNoContent());

        verify(borrarLeidas).borrarLeidas(USUARIO);
    }
}
