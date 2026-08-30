package com.polaris.auth.domain.service;

import com.polaris.auth.application.out.PasswordHasherPort;
import com.polaris.auth.application.out.UsuarioRepositoryPort;
import com.polaris.auth.domain.model.CredencialesInvalidasException;
import com.polaris.auth.domain.model.EmailNoVerificadoException;
import com.polaris.auth.domain.model.PerfilGoogle;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.domain.model.UsuarioNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private PasswordHasherPort passwordHasher;

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

    // ---- registro nativo ----

    @Test
    @DisplayName("registrar guarda la contrasena hasheada, nunca en claro")
    void registrarGuardaContrasenaHasheada() {
        when(repository.findByUsername("daniel")).thenReturn(Optional.empty());
        when(repository.findByEmail("daniel@example.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("clave-secreta-12345")).thenReturn("hash-bcrypt-falso");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = service.registrar("Daniel", "Daniel@Example.com", "clave-secreta-12345");

        assertThat(creado.getUsername()).isEqualTo("daniel");
        assertThat(creado.getEmail()).isEqualTo("daniel@example.com");
        assertThat(creado.getPasswordHash())
                .isEqualTo("hash-bcrypt-falso")
                .isNotEqualTo("clave-secreta-12345");
        assertThat(creado.isEmailVerificado()).isFalse();
        assertThat(creado.getCreadoEn()).isNotNull();
    }

    @Test
    @DisplayName("registrar rechaza un username ya usado sin llegar a comprobar el email")
    void registrarRechazaUsernameDuplicado() {
        when(repository.findByUsername("daniel")).thenReturn(Optional.of(Usuario.builder().id(1L).build()));

        assertThatThrownBy(() -> service.registrar("daniel", "otro@example.com", "clave-secreta-12345"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).findByEmail(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("registrar rechaza un email ya usado")
    void registrarRechazaEmailDuplicado() {
        when(repository.findByUsername("daniel")).thenReturn(Optional.empty());
        when(repository.findByEmail("daniel@example.com")).thenReturn(Optional.of(Usuario.builder().id(1L).build()));

        assertThatThrownBy(() -> service.registrar("daniel", "daniel@example.com", "clave-secreta-12345"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    // ---- login nativo ----

    @Test
    @DisplayName("login por username, con credenciales correctas y email verificado")
    void loginConUsername() {
        Usuario usuario = Usuario.builder()
                .id(9L).username("daniel").email("daniel@example.com")
                .passwordHash("hash").emailVerificado(true).build();

        when(repository.findByUsername("daniel")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("clave", "hash")).thenReturn(true);

        assertThat(service.login("Daniel", "clave").getId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("login por email cuando no coincide como username")
    void loginConEmail() {
        Usuario usuario = Usuario.builder()
                .id(9L).username("daniel").email("daniel@example.com")
                .passwordHash("hash").emailVerificado(true).build();

        when(repository.findByUsername("daniel@example.com")).thenReturn(Optional.empty());
        when(repository.findByEmail("daniel@example.com")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("clave", "hash")).thenReturn(true);

        assertThat(service.login("daniel@example.com", "clave").getId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("login lanza CredencialesInvalidasException si el usuario no existe")
    void loginLanzaSiNoExiste() {
        when(repository.findByUsername("fantasma")).thenReturn(Optional.empty());
        when(repository.findByEmail("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("fantasma", "clave"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("login lanza CredencialesInvalidasException si la contrasena no coincide")
    void loginLanzaSiContrasenaIncorrecta() {
        Usuario usuario = Usuario.builder().id(9L).username("daniel").passwordHash("hash").emailVerificado(true).build();
        when(repository.findByUsername("daniel")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login("daniel", "mala"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("login lanza CredencialesInvalidasException en una cuenta solo-Google, sin llamar al hasher")
    void loginLanzaSiCuentaEsSoloDeGoogle() {
        Usuario usuario = Usuario.builder().id(9L).username("daniel").passwordHash(null).emailVerificado(true).build();
        when(repository.findByUsername("daniel")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.login("daniel", "clave"))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(passwordHasher, never()).coincide(anyString(), anyString());
    }

    @Test
    @DisplayName("login lanza EmailNoVerificadoException con credenciales correctas pero email sin verificar")
    void loginLanzaSiEmailNoVerificado() {
        Usuario usuario = Usuario.builder().id(9L).username("daniel").passwordHash("hash").emailVerificado(false).build();
        when(repository.findByUsername("daniel")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("clave", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login("daniel", "clave"))
                .isInstanceOf(EmailNoVerificadoException.class);
    }

    // ---- verificacion de email ----

    @Test
    @DisplayName("verificar marca el email como verificado")
    void verificarMarcaEmailVerificado() {
        Usuario usuario = Usuario.builder().id(9L).emailVerificado(false).build();
        when(repository.findById(9L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.verificar(9L).isEmailVerificado()).isTrue();
    }

    // ---- vinculacion de cuentas por email ----

    @Test
    @DisplayName("un login de Google con el email de una cuenta nativa la vincula en vez de duplicarla")
    void loginDeGoogleVinculaCuentaNativaExistente() {
        Usuario nativo = Usuario.builder()
                .id(3L).username("daniel").email("daniel@example.com")
                .passwordHash("hash-nativo").emailVerificado(false).build();

        when(repository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(repository.findByEmail("daniel@example.com")).thenReturn(Optional.of(nativo));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario vinculado = service.getOrCreate(
                new PerfilGoogle(GOOGLE_ID, "daniel@example.com", "Daniel", "https://foto"));

        assertThat(vinculado.getId()).isEqualTo(3L);
        assertThat(vinculado.getGoogleId()).isEqualTo(GOOGLE_ID);
        assertThat(vinculado.isEmailVerificado()).isTrue();
        // El login nativo sigue funcionando: el hash no se toca al vincular.
        assertThat(vinculado.getPasswordHash()).isEqualTo("hash-nativo");
    }

    @Test
    @DisplayName("el username de un usuario nuevo de Google se deriva de la parte local del email")
    void primerLoginDeGoogleDerivaUsernameDelEmail() {
        when(repository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(repository.findByEmail("daniel@example.com")).thenReturn(Optional.empty());
        when(repository.findByUsername("daniel")).thenReturn(Optional.empty());
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = service.getOrCreate(
                new PerfilGoogle(GOOGLE_ID, "daniel@example.com", "Daniel", null));

        assertThat(creado.getUsername()).isEqualTo("daniel");
    }

    @Test
    @DisplayName("si el username derivado ya existe, se le anade un sufijo numerico")
    void primerLoginDeGoogleAnadeSufijoSiUsernameOcupado() {
        when(repository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(repository.findByEmail("daniel@example.com")).thenReturn(Optional.empty());
        when(repository.findByUsername("daniel")).thenReturn(Optional.of(Usuario.builder().id(1L).build()));
        when(repository.findByUsername("daniel2")).thenReturn(Optional.empty());
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = service.getOrCreate(
                new PerfilGoogle(GOOGLE_ID, "daniel@example.com", "Daniel", null));

        assertThat(creado.getUsername()).isEqualTo("daniel2");
    }
}
