package com.polaris.auth.domain.service;

import com.polaris.auth.application.out.UsuarioRepositoryPort;
import com.polaris.auth.domain.model.PerfilGoogle;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.domain.model.UsuarioNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Servicio de dominio con el puerto mockeado, que es donde docs/convenciones.md
 * pone la prioridad de los tests.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String GOOGLE_ID = "112233445566778899";

    @Mock
    private UsuarioRepositoryPort repository;

    @InjectMocks
    private UsuarioService service;

    @Test
    @DisplayName("get devuelve el usuario cuando existe")
    void getDevuelveUsuarioExistente() {
        Usuario guardado = Usuario.builder().id(1L).email("daniel@example.com").build();
        when(repository.findById(1L)).thenReturn(Optional.of(guardado));

        assertThat(service.get(1L).getEmail()).isEqualTo("daniel@example.com");
    }

    @Test
    @DisplayName("get lanza UsuarioNotFoundException cuando no existe")
    void getLanzaCuandoNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("el primer login da de alta al usuario con los datos de Google")
    void primerLoginCreaUsuario() {
        when(repository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        PerfilGoogle perfil = new PerfilGoogle(GOOGLE_ID, "daniel@example.com", "Daniel", "https://foto");
        Usuario creado = service.getOrCreate(perfil);

        assertThat(creado.getGoogleId()).isEqualTo(GOOGLE_ID);
        assertThat(creado.getEmail()).isEqualTo("daniel@example.com");
        assertThat(creado.getNombre()).isEqualTo("Daniel");
        assertThat(creado.getAvatarUrl()).isEqualTo("https://foto");
        assertThat(creado.getCreadoEn()).isNotNull();
    }

    @Test
    @DisplayName("un login posterior reutiliza el usuario y refresca nombre y avatar")
    void loginPosteriorRefrescaDatos() {
        Usuario existente = Usuario.builder()
                .id(7L)
                .googleId(GOOGLE_ID)
                .email("viejo@example.com")
                .nombre("Nombre viejo")
                .avatarUrl("https://foto-vieja")
                .creadoEn(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        when(repository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.of(existente));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.getOrCreate(new PerfilGoogle(GOOGLE_ID, "nuevo@example.com", "Nombre nuevo", "https://foto-nueva"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertThat(guardado.getId()).isEqualTo(7L);
        assertThat(guardado.getNombre()).isEqualTo("Nombre nuevo");
        assertThat(guardado.getAvatarUrl()).isEqualTo("https://foto-nueva");
        assertThat(guardado.getEmail()).isEqualTo("nuevo@example.com");
        // La fecha de alta no se toca en logins posteriores
        assertThat(guardado.getCreadoEn()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("el email que cambia en Google no crea un usuario duplicado")
    void emailDistintoNoDuplicaUsuario() {
        Usuario existente = Usuario.builder().id(7L).googleId(GOOGLE_ID).email("viejo@example.com").build();
        when(repository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.of(existente));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.getOrCreate(
                new PerfilGoogle(GOOGLE_ID, "otro@example.com", "Daniel", null));

        assertThat(resultado.getId()).isEqualTo(7L);
        verify(repository, never()).findById(any());
    }
}
