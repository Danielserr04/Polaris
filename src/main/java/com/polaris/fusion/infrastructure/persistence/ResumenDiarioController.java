package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.GetResumenDiarioInterface;
import com.polaris.fusion.infrastructure.persistence.dto.in.ResumenDiarioFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ResumenDiarioDto;
import com.polaris.fusion.infrastructure.persistence.mapper.ResumenDiarioDtoMapper;
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

import java.time.LocalDate;

/**
 * Solo lectura: el resumen no es una entidad, se calcula. Sin {@code fecha}
 * usa hoy. Inyecta la interfaz de caso de uso, no el Service.
 */
@Tag(name = "Fusion - Resumen diario",
     description = "Resumen nutricional de un dia calculado sobre tus comidas y tu objetivo. Solo lectura.")
@RestController
@RequestMapping("/api/fusion/resumen")
@RequiredArgsConstructor
public class ResumenDiarioController {

    private final GetResumenDiarioInterface getResumenDiario;
    private final ResumenDiarioDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Resumen de un dia",
            description = "Suma de las comidas del dia frente al objetivo vigente ese dia. Sin objetivo vigente "
                    + "no es un error: se devuelve lo consumido y objetivo, restante y porcentaje van a "
                    + "null. Sin fecha usa hoy.")
    @ApiResponse(responseCode = "200", description = "Resumen del dia")
    @ApiResponse(responseCode = "400", description = "fecha no tiene el formato yyyy-MM-dd")
    @GetMapping
    public ResponseEntity<ResumenDiarioDto> get(@ParameterObject ResumenDiarioFilterListDto filtro) {
        LocalDate fecha = filtro.fecha() != null ? filtro.fecha() : LocalDate.now();
        return ResponseEntity.ok(dtoMapper.toDto(getResumenDiario.get(usuarioActual.id(), fecha)));
    }
}
