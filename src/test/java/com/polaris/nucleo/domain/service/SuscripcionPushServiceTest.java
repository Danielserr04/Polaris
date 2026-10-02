package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.out.ClavePushPort;
import com.polaris.nucleo.application.out.SuscripcionPushRepositoryPort;
import com.polaris.nucleo.domain.model.SuscripcionPush;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El endpoint identifica al dispositivo: repetir la suscripcion actualiza la
 * fila, y nadie puede dar de baja el dispositivo de otro.
 */
@ExtendWith(MockitoExtension.class)
class SuscripcionPushServiceTest {

    private static final Long USUARIO = 1L;
    private static final String ENDPOINT = "https://fcm.googleapis.com/fcm/send/abc";

    @Mock
    private SuscripcionPushRepositoryPort repository;
    @Mock
    private ClavePushPort clave;
    @InjectMocks
    private SuscripcionPushService service;

    private static SuscripcionPush nueva() {
        return SuscripcionPush.builder().endpoint(ENDPOINT).p256dh("p").auth("a").build();
    }

    @Test
    @DisplayName("create da de alta con el usuario del JWT y fecha de alta")
    void createNueva() {
        when(repository.findByEndpoint(ENDPOINT)).thenReturn(Optional.empty());
        when(repository.save(any(SuscripcionPush.class))).thenAnswer(inv -> inv.getArgument(0));

        SuscripcionPush s = service.create(USUARIO, nueva());

        assertThat(s.getId()).isNull();
        assertThat(s.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(s.getCreadaEn()).isNotNull();
    }

    @Test
    @DisplayName("create con un endpoint ya guardado reutiliza la fila y la pasa al usuario actual")
    void createExistente() {
        LocalDateTime alta = LocalDateTime.of(2026, 9, 1, 10, 0);
        when(repository.findByEndpoint(ENDPOINT)).thenReturn(Optional.of(SuscripcionPush.builder()
                .id(4L).usuarioId(99L).endpoint(ENDPOINT).p256dh("viejo").auth("viejo").creadaEn(alta).build()));
        when(repository.save(any(SuscripcionPush.class))).thenAnswer(inv -> inv.getArgument(0));

        SuscripcionPush s = service.create(USUARIO, nueva());

        assertThat(s.getId()).isEqualTo(4L);
        assertThat(s.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(s.getP256dh()).isEqualTo("p");
        assertThat(s.getCreadaEn()).isEqualTo(alta);
    }

    @Test
    @DisplayName("delete no toca el dispositivo de otro usuario")
    void deleteDeOtro() {
        when(repository.findByEndpoint(ENDPOINT)).thenReturn(Optional.of(SuscripcionPush.builder()
                .id(4L).usuarioId(99L).endpoint(ENDPOINT).build()));

        service.delete(USUARIO, ENDPOINT);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete borra el dispositivo propio")
    void deletePropio() {
        when(repository.findByEndpoint(ENDPOINT)).thenReturn(Optional.of(SuscripcionPush.builder()
                .id(4L).usuarioId(USUARIO).endpoint(ENDPOINT).build()));

        service.delete(USUARIO, ENDPOINT);

        verify(repository).deleteById(4L);
    }
}
