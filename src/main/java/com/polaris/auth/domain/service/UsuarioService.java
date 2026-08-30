package com.polaris.auth.domain.service;

import com.polaris.auth.application.in.ActualizarPerfilInterface;
import com.polaris.auth.application.in.CambiarEmailInterface;
import com.polaris.auth.application.in.CambiarPasswordInterface;
import com.polaris.auth.application.in.DesvincularGoogleInterface;
import com.polaris.auth.application.in.GetOrCreateUsuarioInterface;
import com.polaris.auth.application.in.GetUsuarioInterface;
import com.polaris.auth.application.in.LoginInterface;
import com.polaris.auth.application.in.RegistrarUsuarioInterface;
import com.polaris.auth.application.in.VerificarEmailInterface;
import com.polaris.auth.application.out.PasswordHasherPort;
import com.polaris.auth.application.out.UsuarioRepositoryPort;
import com.polaris.auth.domain.model.CredencialesInvalidasException;
import com.polaris.auth.domain.model.EmailNoVerificadoException;
import com.polaris.auth.domain.model.PerfilGoogle;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.domain.model.UsuarioNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Aqui vive la logica de auth. No sabe que existe HTTP, OAuth2, JPA ni BCrypt:
 * solo puertos y modelo.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements
        GetUsuarioInterface,
        GetOrCreateUsuarioInterface,
        RegistrarUsuarioInterface,
        LoginInterface,
        VerificarEmailInterface,
        ActualizarPerfilInterface,
        CambiarPasswordInterface,
        CambiarEmailInterface,
        DesvincularGoogleInterface {

    private final UsuarioRepositoryPort repository;
    private final PasswordHasherPort passwordHasher;

    @Override
    public Usuario get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    /**
     * El googleId es la identidad estable, no el email: Google permite cambiar
     * la direccion de una cuenta sin que cambie el identificador.
     *
     * <p>En cada login se refrescan nombre y avatar, que el usuario puede
     * haber cambiado en su cuenta de Google desde la ultima vez.
     */
    @Override
    public Usuario getOrCreate(PerfilGoogle perfil) {
        return repository.findByGoogleId(perfil.googleId())
                .map(existente -> actualizarDatosDeGoogle(existente, perfil))
                .orElseGet(() -> vincularOCrear(perfil));
    }

    @Override
    public Usuario registrar(String username, String email, String password) {
        String usernameNormalizado = normalizar(username);
        String emailNormalizado = normalizar(email);

        if (repository.findByUsername(usernameNormalizado).isPresent()) {
            throw new DuplicateResourceException("Ese nombre de usuario ya esta en uso");
        }
        if (repository.findByEmail(emailNormalizado).isPresent()) {
            throw new DuplicateResourceException("Ese email ya esta registrado");
        }

        Usuario nuevo = Usuario.builder()
                .username(usernameNormalizado)
                .email(emailNormalizado)
                .nombre(username)
                .passwordHash(passwordHasher.hash(password))
                .emailVerificado(false)
                .creadoEn(Instant.now())
                .build();

        return repository.save(nuevo);
    }

    /**
     * Mismo mensaje si el usuario no existe, si es una cuenta solo-Google sin
     * contrasena, o si la contrasena no coincide: distinguirlos le diria a
     * cualquiera que correos estan dados de alta.
     */
    @Override
    public Usuario login(String usernameOEmail, String password) {
        String normalizado = normalizar(usernameOEmail);

        Usuario usuario = repository.findByUsername(normalizado)
                .or(() -> repository.findByEmail(normalizado))
                .filter(candidato -> candidato.getPasswordHash() != null)
                .filter(candidato -> passwordHasher.coincide(password, candidato.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isEmailVerificado()) {
            throw new EmailNoVerificadoException();
        }

        return usuario;
    }

    @Override
    public Usuario verificar(Long usuarioId) {
        Usuario usuario = get(usuarioId);
        usuario.setEmailVerificado(true);
        return repository.save(usuario);
    }

    /**
     * Nombre y avatar son lo unico editable del perfil. El username no: es la
     * mitad de tus credenciales, y cambiarlo dejaria de funcionar el login
     * mientras el usuario cree que sigue igual. El email tiene su propio caso
     * de uso porque obliga a re-verificar.
     *
     * <p>Ojo con Google: si la cuenta esta vinculada, el proximo login con
     * Google vuelve a pisar nombre y avatar con los de Google. Es deliberado
     * (ver getOrCreate), pero significa que editarlos aqui solo dura hasta
     * entonces.
     */
    @Override
    public Usuario actualizarPerfil(Long usuarioId, String nombre, String avatarUrl) {
        Usuario usuario = get(usuarioId);
        usuario.setNombre(nombre);
        usuario.setAvatarUrl(avatarUrl);
        return repository.save(usuario);
    }

    /**
     * Cambiar la contrasena exige la actual, para que un token robado no baste
     * para secuestrar la cuenta.
     *
     * <p>La excepcion es una cuenta que entro solo con Google y todavia no
     * tiene contrasena: ahi no hay actual que pedir, y se esta anadiendo una
     * segunda forma de entrar, no sustituyendo la que hay.
     */
    @Override
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) {
        Usuario usuario = get(usuarioId);

        if (usuario.getPasswordHash() != null) {
            if (passwordActual == null
                    || !passwordHasher.coincide(passwordActual, usuario.getPasswordHash())) {
                throw new CredencialesInvalidasException();
            }
        }

        usuario.setPasswordHash(passwordHasher.hash(passwordNueva));
        repository.save(usuario);
    }

    /**
     * Cambiar el email deja la cuenta <b>sin verificar</b>: el correo nuevo no
     * lo ha comprobado nadie. Quien llame se encarga de mandar el enlace.
     *
     * <p>Pide la contrasena actual por lo mismo que cambiarPassword: si no, un
     * token robado permitiria apuntar la cuenta a otro correo y quedarsela. Una
     * cuenta solo-Google no tiene contrasena que pedir, y ahi el propio login
     * de Google es la garantia.
     */
    @Override
    public Usuario cambiarEmail(Long usuarioId, String emailNuevo, String password) {
        Usuario usuario = get(usuarioId);

        if (usuario.getPasswordHash() != null) {
            if (password == null || !passwordHasher.coincide(password, usuario.getPasswordHash())) {
                throw new CredencialesInvalidasException();
            }
        }

        String normalizado = normalizar(emailNuevo);

        if (normalizado.equals(usuario.getEmail())) {
            throw new ValidationException("Ese ya es tu email");
        }
        if (repository.findByEmail(normalizado).isPresent()) {
            throw new DuplicateResourceException("Ese email ya esta registrado");
        }

        usuario.setEmail(normalizado);
        usuario.setEmailVerificado(false);
        return repository.save(usuario);
    }

    /**
     * Desvincular Google solo se permite si queda otra forma de entrar. Sin
     * contrasena, quitarle el googleId a la cuenta la deja inaccesible para
     * siempre: no hay nada con lo que volver a autenticarse.
     */
    @Override
    public Usuario desvincularGoogle(Long usuarioId) {
        Usuario usuario = get(usuarioId);

        if (usuario.getGoogleId() == null) {
            throw new ValidationException("Esta cuenta no esta vinculada con Google");
        }
        if (usuario.getPasswordHash() == null) {
            throw new ValidationException(
                    "Pon una contrasena antes de desvincular Google, o te quedas sin forma de entrar");
        }

        usuario.setGoogleId(null);
        return repository.save(usuario);
    }

    private Usuario actualizarDatosDeGoogle(Usuario usuario, PerfilGoogle perfil) {
        usuario.setEmail(perfil.email());
        usuario.setNombre(perfil.nombre());
        usuario.setAvatarUrl(perfil.avatarUrl());
        return repository.save(usuario);
    }

    /**
     * Si ya existe una cuenta nativa con ese email, se vincula en vez de
     * duplicar: Google ya ha verificado el correo, que es prueba mas fuerte
     * que nuestra propia verificacion, asi que tambien marca emailVerificado.
     * El passwordHash existente no se toca: el login nativo sigue funcionando.
     */
    private Usuario vincularOCrear(PerfilGoogle perfil) {
        return repository.findByEmail(normalizar(perfil.email()))
                .map(existente -> vincular(existente, perfil))
                .orElseGet(() -> crear(perfil));
    }

    private Usuario vincular(Usuario existente, PerfilGoogle perfil) {
        existente.setGoogleId(perfil.googleId());
        existente.setNombre(perfil.nombre());
        existente.setAvatarUrl(perfil.avatarUrl());
        existente.setEmailVerificado(true);
        return repository.save(existente);
    }

    private Usuario crear(PerfilGoogle perfil) {
        Usuario nuevo = Usuario.builder()
                .username(derivarUsername(perfil.email()))
                .googleId(perfil.googleId())
                .email(normalizar(perfil.email()))
                .nombre(perfil.nombre())
                .avatarUrl(perfil.avatarUrl())
                .emailVerificado(true)
                .creadoEn(Instant.now())
                .build();

        return repository.save(nuevo);
    }

    /** Parte local del email, saneada, con sufijo numerico si ya existe. */
    private String derivarUsername(String email) {
        String base = normalizar(email.substring(0, email.indexOf('@')))
                .replaceAll("[^a-z0-9._-]", "");
        if (base.isBlank()) {
            base = "usuario";
        }

        String candidato = base;
        int sufijo = 1;
        while (repository.findByUsername(candidato).isPresent()) {
            sufijo++;
            candidato = base + sufijo;
        }
        return candidato;
    }

    /** Username y email se comparan y guardan en minusculas: login insensible a mayusculas. */
    private String normalizar(String valor) {
        return valor.trim().toLowerCase();
    }
}
