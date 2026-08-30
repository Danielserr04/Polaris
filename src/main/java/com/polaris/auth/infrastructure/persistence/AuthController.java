package com.polaris.auth.infrastructure.persistence;

import com.polaris.auth.application.in.GetUsuarioInterface;
import com.polaris.auth.application.in.LoginInterface;
import com.polaris.auth.application.in.RegistrarUsuarioInterface;
import com.polaris.auth.application.in.VerificarEmailInterface;
import com.polaris.auth.application.out.EnviarVerificacionPort;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.dto.in.LoginRequestDto;
import com.polaris.auth.infrastructure.persistence.dto.in.RegistroRequestDto;
import com.polaris.auth.infrastructure.persistence.dto.out.TokenDto;
import com.polaris.auth.infrastructure.persistence.dto.out.UsuarioFormDto;
import com.polaris.auth.infrastructure.persistence.mapper.UsuarioFormDtoMapper;
import com.polaris.auth.infrastructure.security.JwtService;
import com.polaris.shared.error.ValidationException;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GetUsuarioInterface getUsuario;
    private final RegistrarUsuarioInterface registrarUsuario;
    private final LoginInterface loginUsuario;
    private final VerificarEmailInterface verificarEmail;
    private final UsuarioFormDtoMapper mapper;
    private final UsuarioActual usuarioActual;
    private final JwtService jwtService;
    private final EnviarVerificacionPort enviarVerificacion;

    /**
     * Entregable de B1: endpoint protegido que devuelve tu usuario.
     */
    @GetMapping("/usuario")
    @Operation(summary = "Devuelve el usuario autenticado")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<UsuarioFormDto> getUsuarioAutenticado() {
        return ResponseEntity.ok(mapper.toFormDto(getUsuario.get(usuarioActual.id())));
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registro nativo. Manda un email de verificacion antes de poder entrar")
    public void registro(@Valid @RequestBody RegistroRequestDto dto) {
        Usuario usuario = registrarUsuario.registrar(dto.username(), dto.email(), dto.password());
        String token = jwtService.generarVerificacion(usuario.getId());
        enviarVerificacion.enviar(usuario.getEmail(), usuario.getNombre(), token);
    }

    @PostMapping("/login")
    @Operation(summary = "Login nativo por username o email")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginRequestDto dto) {
        Usuario usuario = loginUsuario.login(dto.usernameOEmail(), dto.password());
        return ResponseEntity.ok(TokenDto.bearer(jwtService.generar(usuario.getId()), jwtService.getExpiracionSegundos()));
    }

    @GetMapping("/verificacion")
    @Operation(summary = "Confirma el email a partir del enlace del registro")
    public ResponseEntity<Void> verificacion(@RequestParam String token) {
        Long usuarioId = jwtService.validarTokenVerificacion(token)
                .orElseThrow(() -> new ValidationException("Enlace de verificacion invalido o caducado"));
        verificarEmail.verificar(usuarioId);
        return ResponseEntity.ok().build();
    }
}
