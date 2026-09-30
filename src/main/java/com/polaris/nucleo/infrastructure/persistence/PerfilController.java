package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.GetPerfilInterface;
import com.polaris.nucleo.application.in.UpdatePerfilInterface;
import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.infrastructure.persistence.dto.in.PerfilRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.PerfilFormDto;
import com.polaris.nucleo.infrastructure.persistence.mapper.PerfilFormDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.PerfilRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sin {id} en la ruta: el perfil es siempre el del usuario del JWT. Solo GET
 * y PUT (que crea si no existe). Ver
 * docs/decisiones/009-perfil-unico-por-usuario.md.
 */
@RestController
@RequestMapping("/api/nucleo/perfil")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PerfilController {

    private final GetPerfilInterface getPerfil;
    private final UpdatePerfilInterface updatePerfil;
    private final PerfilRequestDtoMapper requestDtoMapper;
    private final PerfilFormDtoMapper formDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<PerfilFormDto> get() {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getPerfil.get(usuarioActual.id())));
    }

    @PutMapping
    public ResponseEntity<PerfilFormDto> update(@Valid @RequestBody PerfilRequestDto dto) {
        Perfil guardado = updatePerfil.update(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(guardado));
    }
}
