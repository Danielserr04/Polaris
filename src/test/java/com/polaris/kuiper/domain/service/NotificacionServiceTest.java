package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.NotificacionRepositoryPort;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.NotificacionNotFoundException;
import com.polaris.kuiper.domain.model.TipoNotificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: el mismo hecho no se avisa dos veces, crear nunca
 * lanza aunque la base de datos falle, y un usuario nunca ve ni toca las
 * notificaciones de otro. Ver docs/decisiones/040-notificaciones-de-kuiper.md.
 */
@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private NotificacionRepositoryPort repository;

    @InjectMocks
    private NotificacionService service;

    private static Notificacion nueva(String clave) {
        return Notificacion.builder().id(99L).usuarioId(USUARIO).tipo(TipoNotificacion.CARGO_PROXIMO)
                .clave(clave).titulo("Cargo proximo: Netflix").texto("12,99 el 05/10/2026.").leida(true).build();
    }

    private static Notificacion guardada(Long id, Long usuarioId, boolean leida) {
        return Notificacion.builder().id(id).usuarioId(usuarioId).tipo(TipoNotificacion.RESUMEN_MENSUAL)
                .clave("resumen-2026-09").titulo("Resumen").texto("...").leida(leida).build();
    }

    @Test
    @DisplayName("crear guarda sin id, sin leer y con fecha de creacion")
    void crearGuardaNueva() {
        when(repository.existsByUsuarioIdAndClave(USUARIO, "proximo-4-2026-10-05")).thenReturn(false);
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Notificacion> creada = service.crear(nueva("proximo-4-2026-10-05"));

        assertThat(creada).isPresent();
        assertThat(creada.get().getId()).isNull();
        assertThat(creada.get().isLeida()).isFalse();
        assertThat(creada.get().getCreadaEn()).isNotNull();
    }

    @Test
    @DisplayName("crear no duplica: si la clave ya existe para el usuario no guarda y devuelve vacio")
    void crearEsIdempotentePorClave() {
        when(repository.existsByUsuarioIdAndClave(USUARIO, "proximo-4-2026-10-05")).thenReturn(true);

        assertThat(service.crear(nueva("proximo-4-2026-10-05"))).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crear nunca lanza: si guardar falla devuelve vacio")
    void crearNoLanzaSiFallaElGuardado() {
        when(repository.existsByUsuarioIdAndClave(any(), any())).thenReturn(false);
        when(repository.save(any(Notificacion.class))).thenThrow(new IllegalStateException("Duplicate entry"));

        assertThat(service.crear(nueva("proximo-4-2026-10-05"))).isEmpty();
    }

    @Test
    @DisplayName("crear nunca lanza: tampoco si falla la comprobacion de la clave")
    void crearNoLanzaSiFallaLaComprobacion() {
        when(repository.existsByUsuarioIdAndClave(any(), any())).thenThrow(new IllegalStateException("BD caida"));

        assertThat(service.crear(nueva("x"))).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crear corta titulo y texto a lo que cabe en sus columnas")
    void crearCortaLosTextosLargos() {
        when(repository.existsByUsuarioIdAndClave(any(), any())).thenReturn(false);
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));
        Notificacion larga = nueva("clave");
        larga.setTitulo("t".repeat(400));
        larga.setTexto("x".repeat(900));

        Notificacion creada = service.crear(larga).orElseThrow();

        assertThat(creada.getTitulo()).hasSize(NotificacionService.MAX_TITULO);
        assertThat(creada.getTexto()).hasSize(NotificacionService.MAX_TEXTO);
    }

    @Test
    @DisplayName("get de una notificacion de otro usuario da 404, no 403")
    void getDeOtroUsuarioEs404() {
        when(repository.findById(5L)).thenReturn(Optional.of(guardada(5L, OTRO_USUARIO, false)));

        assertThatThrownBy(() -> service.get(USUARIO, 5L)).isInstanceOf(NotificacionNotFoundException.class);
    }

    @Test
    @DisplayName("update solo cambia leida, aunque los cambios traigan mas campos")
    void updateSoloCambiaLeida() {
        when(repository.findById(5L)).thenReturn(Optional.of(guardada(5L, USUARIO, false)));
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion cambios = Notificacion.builder().leida(true).titulo("otro").usuarioId(OTRO_USUARIO).build();
        service.update(USUARIO, 5L, cambios);

        ArgumentCaptor<Notificacion> guardadaCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository).save(guardadaCaptor.capture());
        assertThat(guardadaCaptor.getValue().isLeida()).isTrue();
        assertThat(guardadaCaptor.getValue().getTitulo()).isEqualTo("Resumen");
        assertThat(guardadaCaptor.getValue().getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("update sin cambio real no escribe")
    void updateSinCambioNoGuarda() {
        when(repository.findById(5L)).thenReturn(Optional.of(guardada(5L, USUARIO, true)));

        Notificacion resultado = service.update(USUARIO, 5L, Notificacion.builder().leida(true).build());

        assertThat(resultado.isLeida()).isTrue();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update de una notificacion de otro usuario da 404 y no guarda")
    void updateDeOtroUsuarioEs404() {
        when(repository.findById(5L)).thenReturn(Optional.of(guardada(5L, OTRO_USUARIO, false)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, Notificacion.builder().leida(true).build()))
                .isInstanceOf(NotificacionNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete de una notificacion de otro usuario da 404 y no borra")
    void deleteDeOtroUsuarioEs404() {
        when(repository.findById(5L)).thenReturn(Optional.of(guardada(5L, OTRO_USUARIO, false)));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L)).isInstanceOf(NotificacionNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete de una propia la borra")
    void deletePropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(guardada(5L, USUARIO, false)));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("contar, leer todas y borrar leidas van siempre filtradas por el usuario")
    void operacionesMasivasPorUsuario() {
        when(repository.countNoLeidas(USUARIO)).thenReturn(3L);
        when(repository.marcarTodasLeidas(USUARIO)).thenReturn(3);
        when(repository.deleteLeidas(USUARIO)).thenReturn(2);

        assertThat(service.contarNoLeidas(USUARIO)).isEqualTo(3L);
        assertThat(service.leerTodas(USUARIO)).isEqualTo(3);
        assertThat(service.borrarLeidas(USUARIO)).isEqualTo(2);
    }
}
