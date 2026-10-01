package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.GetPerfilInterface;
import com.polaris.nucleo.application.in.UpdatePerfilInterface;
import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.infrastructure.persistence.dto.in.PerfilRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.PerfilFormDto;
import com.polaris.nucleo.infrastructure.persistence.mapper.PerfilFormDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.PerfilRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Nucleo - Perfil",
     description = "Perfil del usuario autenticado: altura, fecha de nacimiento, sexo y nivel de actividad. Hay "
             + "uno solo por usuario.")
@RestController
@RequestMapping("/api/nucleo/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final GetPerfilInterface getPerfil;
    private final UpdatePerfilInterface updatePerfil;
    private final PerfilRequestDtoMapper requestDtoMapper;
    private final PerfilFormDtoMapper formDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Devuelve tu perfil",
            description = "Es siempre el del usuario del token: la ruta no lleva id.")
    @ApiResponse(responseCode = "200", description = "Tu perfil")
    @ApiResponse(responseCode = "404", description = "Todavia no has guardado tu perfil")
    @GetMapping
    public ResponseEntity<PerfilFormDto> get() {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getPerfil.get(usuarioActual.id())));
    }

    @Operation(summary = "Guarda tu perfil",
            description = "Lo crea si no existe y lo reemplaza si existe. Todos los campos son opcionales, asi "
                    + "que se puede guardar a medias.")
    @ApiResponse(responseCode = "200", description = "Perfil guardado")
    @ApiResponse(responseCode = "400",
            description = "Algun campo no es valido: altura fuera de 50-272 cm, fecha de nacimiento no pasada o "
                    + "valor de enum desconocido")
    @PutMapping
    public ResponseEntity<PerfilFormDto> update(@Valid @RequestBody PerfilRequestDto dto) {
        Perfil guardado = updatePerfil.update(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(guardado));
    }
}
