package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.ListRecordsInterface;
import com.polaris.atlas.infrastructure.persistence.dto.out.RecordEjercicioDto;
import com.polaris.atlas.infrastructure.persistence.mapper.RecordEjercicioDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RestController
@RequestMapping("/api/atlas/records")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class RecordController {

    private final ListRecordsInterface listRecords;
    private final RecordEjercicioDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<RecordEjercicioDto>> list() {
        return ResponseEntity.ok(dtoMapper.toDtoList(listRecords.list(usuarioActual.id())));
    }
}
