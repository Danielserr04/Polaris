package com.polaris.auth.domain.service;

import com.polaris.auth.application.out.PasswordHasherPort;
import com.polaris.auth.application.out.UsuarioRepositoryPort;
import com.polaris.auth.domain.model.CredencialesInvalidasException;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * El perfil de cuenta. Lo que mas importa aqui es que ninguna operacion pueda
 * dejar al usuario sin forma de entrar, y que un token robado no baste para
 * quedarse la cuenta. Ver docs/modulos/auth.md.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioPerfilServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private UsuarioRepositoryPort repository;

    @Mock
    private PasswordHasherPort passwordHasher;

    @InjectMocks
    private UsuarioService service;

    private Usuario nativo() {
        return Usuario.builder()
                .id(USUARIO).username("dani").email("dani@example.com").nombre("Dani")
                .passwordHash("hash-actual").emailVerificado(true)
                .build();
    }

    private Usuario soloGoogle() {
        return Usuario.builder()
                .id(USUARIO).username("dani").email("dani@example.com").nombre("Dani")
                .googleId("google-123").emailVerificado(true)
                .build();
    }

    @Test
    @DisplayName("actualizarPerfil cambia nombre y avatar, y no toca nada mas")
    void actualizarPerfilSoloCambiaNombreYAvatar() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario actualizado = service.actualizarPerfil(USUARIO, "Daniel", "https://x/a.png");

        assertThat(actualizado.getNombre()).isEqualTo("Daniel");
        assertThat(actualizado.getAvatarUrl()).isEqualTo("https://x/a.png");
        assertThat(actualizado.getUsername()).isEqualTo("dani");
        assertThat(actualizado.getEmail()).isEqualTo("dani@example.com");
        assertThat(actualizado.getPasswordHash()).isEqualTo("hash-actual");
    }

    @Test
    @DisplayName("cambiarPassword exige la actual y guarda la nueva hasheada")
    void cambiarPasswordGuardaLaNuevaHasheada() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));
        when(passwordHasher.coincide("actual", "hash-actual")).thenReturn(true);
        when(passwordHasher.hash("nueva-larga")).thenReturn("hash-nuevo");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cambiarPassword(USUARIO, "actual", "nueva-larga");

        verify(repository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarPassword con la actual mal es 401 y no guarda")
    void cambiarPasswordConActualMalNoGuarda() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));
        when(passwordHasher.coincide("loquesea", "hash-actual")).thenReturn(false);

        assertThatThrownBy(() -> service.cambiarPassword(USUARIO, "loquesea", "nueva-larga"))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarPassword sin mandar la actual es 401 si la cuenta tiene contrasena")
    void cambiarPasswordSinActualNoGuarda() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));

        assertThatThrownBy(() -> service.cambiarPassword(USUARIO, null, "nueva-larga"))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("una cuenta solo-Google puede poner contrasena sin dar la actual: no tiene")
    void cuentaSoloGooglePuedePonerPassword() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(soloGoogle()));
        when(passwordHasher.hash("nueva-larga")).thenReturn("hash-nuevo");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cambiarPassword(USUARIO, null, "nueva-larga");

        verify(repository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarEmail normaliza, deja la cuenta SIN verificar y exige la contrasena")
    void cambiarEmailDejaLaCuentaSinVerificar() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));
        when(passwordHasher.coincide("actual", "hash-actual")).thenReturn(true);
        when(repository.findByEmail("nuevo@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario actualizado = service.cambiarEmail(USUARIO, "  Nuevo@Example.com ", "actual");

        assertThat(actualizado.getEmail()).isEqualTo("nuevo@example.com");
        assertThat(actualizado.isEmailVerificado()).isFalse();
    }

    @Test
    @DisplayName("cambiarEmail a uno que ya existe es 409")
    void cambiarEmailDuplicadoEs409() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));
        when(passwordHasher.coincide("actual", "hash-actual")).thenReturn(true);
        when(repository.findByEmail("otro@example.com"))
                .thenReturn(Optional.of(Usuario.builder().id(2L).build()));

        assertThatThrownBy(() -> service.cambiarEmail(USUARIO, "otro@example.com", "actual"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEmail al mismo que ya tienes es 400: no tiene sentido desverificarte")
    void cambiarEmailAlMismoEs400() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));
        when(passwordHasher.coincide("actual", "hash-actual")).thenReturn(true);

        assertThatThrownBy(() -> service.cambiarEmail(USUARIO, "DANI@example.com", "actual"))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEmail con la contrasena mal es 401: si no, un token robado se queda la cuenta")
    void cambiarEmailConPasswordMalEs401() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));
        when(passwordHasher.coincide("loquesea", "hash-actual")).thenReturn(false);

        assertThatThrownBy(() -> service.cambiarEmail(USUARIO, "nuevo@example.com", "loquesea"))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("desvincular Google se permite si queda contrasena con la que entrar")
    void desvincularGoogleConPassword() {
        Usuario ambos = nativo();
        ambos.setGoogleId("google-123");
        when(repository.findById(USUARIO)).thenReturn(Optional.of(ambos));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.desvincularGoogle(USUARIO).getGoogleId()).isNull();
    }

    @Test
    @DisplayName("desvincular Google sin contrasena se rechaza: dejaria la cuenta inaccesible")
    void desvincularGoogleSinPasswordSeRechaza() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(soloGoogle()));

        assertThatThrownBy(() -> service.desvincularGoogle(USUARIO))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("desvincular Google en una cuenta que no lo tiene es 400")
    void desvincularGoogleSinVincularEs400() {
        when(repository.findById(USUARIO)).thenReturn(Optional.of(nativo()));

        assertThatThrownBy(() -> service.desvincularGoogle(USUARIO))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }
}
