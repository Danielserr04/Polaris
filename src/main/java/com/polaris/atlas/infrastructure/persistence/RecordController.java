package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.ListRecordsInterface;
import com.polaris.atlas.infrastructure.persistence.dto.out.RecordEjercicioDto;
import com.polaris.atlas.infrastructure.persistence.mapper.RecordEjercicioDtoMapper;
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
 * Solo lectura: los records no son una entidad, se calculan sobre las series.
 * Inyecta la interfaz de caso de uso, no el Service.
 */
@Tag(name = "Atlas - Records",
     description = "Mejores marcas por ejercicio, calculadas sobre tus series. Solo lectura.")
@RestController
@RequestMapping("/api/atlas/records")
@RequiredArgsConstructor
public class RecordController {

    private final ListRecordsInterface listRecords;
    private final RecordEjercicioDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus records",
            description = "Una fila por ejercicio con series registradas, ordenada por nombre de ejercicio: el "
                    + "mayor peso en una serie (con las repeticiones de la serie con mas reps a ese peso) y "
                    + "la sesion de mayor volumen.")
    @ApiResponse(responseCode = "200", description = "Records, vacio si aun no has registrado series")
    @GetMapping
    public ResponseEntity<List<RecordEjercicioDto>> list() {
        return ResponseEntity.ok(dtoMapper.toDtoList(listRecords.list(usuarioActual.id())));
    }
}
