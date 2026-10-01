package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.GetProgresionInterface;
import com.polaris.atlas.infrastructure.persistence.dto.in.ProgresionFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.ProgresionSesionDto;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionSesionDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Solo lectura: la progresion no es una entidad, se calcula sobre las series.
 * Inyecta la interfaz de caso de uso, no el Service.
 */
@Tag(name = "Atlas - Progresion",
     description = "Evolucion de un ejercicio sesion a sesion, calculada sobre tus series. Solo lectura.")
@RestController
@RequestMapping("/api/atlas/progresion")
@RequiredArgsConstructor
public class ProgresionController {

    private final GetProgresionInterface getProgresion;
    private final ProgresionFilterMapper filterMapper;
    private final ProgresionSesionDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Progresion de un ejercicio",
            description = "Una fila por sesion en la que hiciste el ejercicio, por fecha ascendente. El volumen "
                    + "es la suma de repeticiones por peso (0 si fue con el peso corporal). Rango de fechas "
                    + "opcional e inclusivo.")
    @ApiResponse(responseCode = "200", description = "Progresion, vacia si no hay series en el rango")
    @ApiResponse(responseCode = "400",
            description = "Falta ejercicioId, formato no valido, o desde posterior a hasta")
    @ApiResponse(responseCode = "404", description = "El ejercicio no existe, o es propio de otro usuario")
    @GetMapping
    public ResponseEntity<List<ProgresionSesionDto>> get(@Valid @ParameterObject ProgresionFilterListDto filtro) {
        return ResponseEntity.ok(dtoMapper.toDtoList(
                getProgresion.get(usuarioActual.id(), filterMapper.toFilter(filtro))));
    }
}
