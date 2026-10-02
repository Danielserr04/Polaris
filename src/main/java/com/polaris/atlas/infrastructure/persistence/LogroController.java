package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.ListLogrosInterface;
import com.polaris.atlas.infrastructure.persistence.dto.out.LogroDto;
import com.polaris.atlas.infrastructure.persistence.mapper.LogroDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Solo lectura: los logros no se guardan, se calculan. Inyecta la interfaz de
 * caso de uso, no el Service.
 */
@Tag(name = "Atlas - Logros",
     description = "Catalogo fijo de logros con tu progreso, calculado sobre sesiones, series y peso. Solo "
             + "lectura.")
@RestController
@RequestMapping("/api/atlas/logros")
@RequiredArgsConstructor
public class LogroController {

    private final ListLogrosInterface listLogros;
    private final LogroDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Tus logros",
            description = "Todos los del catalogo, conseguidos o no, con lo que llevas de cada uno.")
    @ApiResponse(responseCode = "200", description = "El catalogo con tu progreso")
    @GetMapping
    public ResponseEntity<List<LogroDto>> list() {
        return ResponseEntity.ok(dtoMapper.toDtoList(listLogros.list(usuarioActual.id())));
    }
}
