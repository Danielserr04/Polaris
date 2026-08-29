package com.polaris.auth.infrastructure.persistence;

import com.polaris.auth.application.in.GetUsuarioInterface;
import com.polaris.auth.infrastructure.persistence.dto.out.UsuarioFormDto;
import com.polaris.auth.infrastructure.persistence.mapper.UsuarioFormDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inyecta las interfaces de caso de uso, no el Service.
 */
@RestController
@RequestMapping("/api/auth/usuario")
@RequiredArgsConstructor
public class AuthController {

    private final GetUsuarioInterface getUsuario;
    private final UsuarioFormDtoMapper mapper;
    private final UsuarioActual usuarioActual;

    /**
     * Entregable de B1: endpoint protegido que devuelve tu usuario.
     */
    @GetMapping
    @Operation(summary = "Devuelve el usuario autenticado")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<UsuarioFormDto> getUsuarioAutenticado() {
        return ResponseEntity.ok(mapper.toFormDto(getUsuario.get(usuarioActual.id())));
    }
}
