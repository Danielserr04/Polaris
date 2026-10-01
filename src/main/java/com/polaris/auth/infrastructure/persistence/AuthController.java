package com.polaris.auth.infrastructure.persistence;

import com.polaris.auth.application.in.ActualizarPerfilInterface;
import com.polaris.auth.application.in.CambiarEmailInterface;
import com.polaris.auth.application.in.CambiarPasswordInterface;
import com.polaris.auth.application.in.DesvincularGoogleInterface;
import com.polaris.auth.application.in.GetUsuarioInterface;
import com.polaris.auth.application.in.LoginInterface;
import com.polaris.auth.application.in.RegistrarUsuarioInterface;
import com.polaris.auth.application.in.VerificarEmailInterface;
import com.polaris.auth.application.out.EnviarVerificacionPort;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.dto.in.ActualizarPerfilRequestDto;
import com.polaris.auth.infrastructure.persistence.dto.in.CambiarEmailRequestDto;
import com.polaris.auth.infrastructure.persistence.dto.in.CambiarPasswordRequestDto;
import com.polaris.auth.infrastructure.persistence.dto.in.LoginRequestDto;
import com.polaris.auth.infrastructure.persistence.dto.in.RegistroRequestDto;
import com.polaris.auth.infrastructure.persistence.dto.out.TokenDto;
import com.polaris.auth.infrastructure.persistence.dto.out.UsuarioFormDto;
import com.polaris.auth.infrastructure.persistence.mapper.UsuarioFormDtoMapper;
import com.polaris.auth.infrastructure.security.JwtService;
import com.polaris.shared.error.ValidationException;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inyecta las interfaces de caso de uso, no el Service.
 *
 * <p>registro/login/verificacion son acciones, no entidades, asi que sus rutas
 * se salen del patron /api/&lt;modulo&gt;/&lt;entidad&gt; de docs/convenciones.md.
 * No hay forma honesta de que un login sea un sustantivo.
 */
@Tag(name = "Auth - Cuenta",
     description = "Registro y login nativos, verificacion de email y gestion de tu cuenta. El login con Google "
             + "no pasa por aqui: es el flujo OAuth2 que arranca en /oauth2/authorization/google.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GetUsuarioInterface getUsuario;
    private final RegistrarUsuarioInterface registrarUsuario;
    private final LoginInterface loginUsuario;
    private final VerificarEmailInterface verificarEmail;
    private final ActualizarPerfilInterface actualizarPerfil;
    private final CambiarPasswordInterface cambiarPassword;
    private final CambiarEmailInterface cambiarEmail;
    private final DesvincularGoogleInterface desvincularGoogle;
    private final UsuarioFormDtoMapper mapper;
    private final UsuarioActual usuarioActual;
    private final JwtService jwtService;
    private final EnviarVerificacionPort enviarVerificacion;

    /**
     * Entregable de B1: endpoint protegido que devuelve tu usuario.
     */
    @Operation(summary = "Devuelve el usuario autenticado",
            description = "Los datos de tu cuenta. Nunca incluye la contrasena ni el googleId: solo las "
                    + "banderas tienePassword y tieneGoogle.")
    @ApiResponse(responseCode = "200", description = "Tu cuenta")
    @ApiResponse(responseCode = "404", description = "El usuario del token ya no existe")
    @GetMapping("/usuario")
    public ResponseEntity<UsuarioFormDto> getUsuarioAutenticado() {
        return ResponseEntity.ok(mapper.toFormDto(getUsuario.get(usuarioActual.id())));
    }

    @Operation(summary = "Registra una cuenta nativa",
            description = "Crea la cuenta sin verificar y manda un email con el enlace de verificacion. Hasta "
                    + "abrirlo, el login responde 403. Username y email se guardan en minusculas.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada y email de verificacion enviado")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: username de 3 a 50 caracteres, email valido, contrasena de 8 a 100")
    @ApiResponse(responseCode = "409", description = "El username o el email ya estan registrados")
    @SecurityRequirements
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public void registro(@Valid @RequestBody RegistroRequestDto dto) {
        Usuario usuario = registrarUsuario.registrar(dto.username(), dto.email(), dto.password());
        String token = jwtService.generarVerificacion(usuario.getId());
        enviarVerificacion.enviar(usuario.getEmail(), usuario.getNombre(), token);
    }

    @Operation(summary = "Login nativo por username o email",
            description = "Acepta username o email, indistinto. Devuelve un token Bearer para el boton "
                    + "Authorize. Usuario inexistente, cuenta solo de Google y contrasena incorrecta dan el "
                    + "mismo 401.")
    @ApiResponse(responseCode = "200", description = "Token de acceso")
    @ApiResponse(responseCode = "400", description = "Falta el usuario o la contrasena")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    @ApiResponse(responseCode = "403", description = "El email aun no esta verificado")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginRequestDto dto) {
        Usuario usuario = loginUsuario.login(dto.usernameOEmail(), dto.password());
        return ResponseEntity.ok(TokenDto.bearer(jwtService.generar(usuario.getId()), jwtService.getExpiracionSegundos()));
    }

    @Parameter(name = "token", description = "Token del enlace de verificacion recibido por email",
            in = ParameterIn.QUERY)
    @Operation(summary = "Confirma el email a partir del enlace del registro",
            description = "Es el destino del enlace que llega por email.")
    @ApiResponse(responseCode = "200", description = "Email verificado")
    @ApiResponse(responseCode = "400", description = "Enlace de verificacion invalido o caducado, o falta el token")
    @SecurityRequirements
    @GetMapping("/verificacion")
    public ResponseEntity<Void> verificacion(@RequestParam String token) {
        Long usuarioId = jwtService.validarTokenVerificacion(token)
                .orElseThrow(() -> new ValidationException("Enlace de verificacion invalido o caducado"));
        verificarEmail.verificar(usuarioId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Edita tu nombre y tu avatar",
            description = "El username no se puede cambiar. Si la cuenta esta vinculada con Google, el "
                    + "siguiente login con Google vuelve a pisar nombre y avatar.")
    @ApiResponse(responseCode = "200", description = "Cuenta actualizada")
    @ApiResponse(responseCode = "400", description = "Datos no validos: nombre obligatorio, maximo 255 caracteres")
    @PutMapping("/usuario")
    public ResponseEntity<UsuarioFormDto> actualizarPerfilPropio(
            @Valid @RequestBody ActualizarPerfilRequestDto dto) {
        Usuario usuario = actualizarPerfil.actualizarPerfil(
                usuarioActual.id(), dto.nombre(), dto.avatarUrl());
        return ResponseEntity.ok(mapper.toFormDto(usuario));
    }

    /**
     * 204 y no 200: no se devuelve nada del usuario, y desde luego no un token
     * nuevo. El que tienes sigue valiendo.
     */
    @Operation(summary = "Cambia tu contrasena, o pon una si entraste solo con Google",
            description = "Si ya tienes contrasena hay que enviar la actual. Una cuenta solo de Google puede "
                    + "poner la primera sin ella. No devuelve un token nuevo: el que tienes sigue valiendo.")
    @ApiResponse(responseCode = "204", description = "Contrasena cambiada")
    @ApiResponse(responseCode = "400", description = "La contrasena nueva no es valida: de 8 a 100 caracteres")
    @ApiResponse(responseCode = "401", description = "Falta el token, o la contrasena actual falta o no coincide")
    @PutMapping("/usuario/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPasswordPropia(@Valid @RequestBody CambiarPasswordRequestDto dto) {
        cambiarPassword.cambiarPassword(usuarioActual.id(), dto.passwordActual(), dto.passwordNueva());
    }

    /**
     * Cambiar el email deja la cuenta sin verificar y manda el enlace al nuevo,
     * igual que en el registro. Hasta que lo abras, el login nativo responde
     * 403 — pero el token que ya tienes en la mano sigue siendo valido.
     */
    @Operation(summary = "Cambia tu email. Queda sin verificar y manda el enlace al nuevo",
            description = "Pide tu contrasena si tienes una. El token que ya tienes sigue valiendo, pero el "
                    + "login nativo da 403 hasta que abras el enlace.")
    @ApiResponse(responseCode = "200", description = "Email cambiado; cuenta pendiente de verificar")
    @ApiResponse(responseCode = "400", description = "Email no valido, o es el mismo que ya tienes")
    @ApiResponse(responseCode = "401", description = "Falta el token, o la contrasena falta o no coincide")
    @ApiResponse(responseCode = "409", description = "Ese email ya esta registrado")
    @PutMapping("/usuario/email")
    public ResponseEntity<UsuarioFormDto> cambiarEmailPropio(
            @Valid @RequestBody CambiarEmailRequestDto dto) {
        Usuario usuario = cambiarEmail.cambiarEmail(usuarioActual.id(), dto.email(), dto.password());
        enviarVerificacion.enviar(usuario.getEmail(), usuario.getNombre(),
                jwtService.generarVerificacion(usuario.getId()));
        return ResponseEntity.ok(mapper.toFormDto(usuario));
    }

    /**
     * El hueco que quedaba de B1: si el envio falla o el enlace caduca a las
     * 24 h, sin esto la cuenta se queda sin verificar y sin salida.
     */
    @Operation(summary = "Vuelve a mandarte el email de verificacion",
            description = "Para cuando el envio fallo o el enlace caduco.")
    @ApiResponse(responseCode = "204", description = "Email de verificacion enviado")
    @ApiResponse(responseCode = "400", description = "Tu email ya esta verificado")
    @PostMapping("/usuario/verificacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reenviarVerificacion() {
        Usuario usuario = getUsuario.get(usuarioActual.id());

        if (usuario.isEmailVerificado()) {
            throw new ValidationException("Tu email ya esta verificado");
        }

        enviarVerificacion.enviar(usuario.getEmail(), usuario.getNombre(),
                jwtService.generarVerificacion(usuario.getId()));
    }

    @Operation(summary = "Desvincula Google. Necesitas tener contrasena antes",
            description = "Sin contrasena, la cuenta quedaria sin forma de entrar. Devuelve la cuenta "
                    + "actualizada.")
    @ApiResponse(responseCode = "200", description = "Google desvinculado")
    @ApiResponse(responseCode = "400", description = "La cuenta no esta vinculada con Google, o no tiene contrasena")
    @DeleteMapping("/usuario/google")
    public ResponseEntity<UsuarioFormDto> desvincularGooglePropio() {
        return ResponseEntity.ok(mapper.toFormDto(desvincularGoogle.desvincularGoogle(usuarioActual.id())));
    }
}
