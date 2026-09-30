package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.GetProgresionInterface;
import com.polaris.atlas.infrastructure.persistence.dto.in.ProgresionFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.ProgresionSesionDto;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionSesionDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Solo lectura: la progresion no es una entidad, se calcula sobre las series.
 * Inyecta la interfaz de caso de uso, no el Service.
 */
@RestController
@RequestMapping("/api/atlas/progresion")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class ProgresionController {

    private final GetProgresionInterface getProgresion;
    private final ProgresionFilterMapper filterMapper;
    private final ProgresionSesionDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<ProgresionSesionDto>> get(@Valid ProgresionFilterListDto filtro) {
        return ResponseEntity.ok(dtoMapper.toDtoList(
                getProgresion.get(usuarioActual.id(), filterMapper.toFilter(filtro))));
    }
}
