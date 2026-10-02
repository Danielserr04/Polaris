package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.GetTrabajoMuscularInterface;
import com.polaris.atlas.infrastructure.persistence.dto.in.TrabajoMuscularFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.TrabajoMuscularDto;
import com.polaris.atlas.infrastructure.persistence.mapper.TrabajoMuscularDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.TrabajoMuscularFilterMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Solo lectura: se calcula sobre las series, como la progresion. Inyecta la
 * interfaz de caso de uso, no el Service.
 */
@Tag(name = "Atlas - Trabajo muscular",
     description = "Series, sesiones y volumen por grupo muscular en un rango de fechas. Alimenta el mapa "
             + "muscular. Solo lectura.")
@RestController
@RequestMapping("/api/atlas/trabajo-muscular")
@RequiredArgsConstructor
public class TrabajoMuscularController {

    private final GetTrabajoMuscularInterface getTrabajoMuscular;
    private final TrabajoMuscularFilterMapper filterMapper;
    private final TrabajoMuscularDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Trabajo por grupo muscular",
            description = "Una fila por grupo muscular con series tuyas en el rango (inclusivo, opcional), del "
                    + "que tiene mas series al que menos. El grupo es el texto del ejercicio tal cual.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si no hay series en el rango")
    @ApiResponse(responseCode = "400", description = "Formato de fecha no valido, o desde posterior a hasta")
    @GetMapping
    public ResponseEntity<List<TrabajoMuscularDto>> get(@ParameterObject TrabajoMuscularFilterListDto filtro) {
        return ResponseEntity.ok(dtoMapper.toDtoList(
                getTrabajoMuscular.get(usuarioActual.id(), filterMapper.toFilter(filtro))));
    }
}
