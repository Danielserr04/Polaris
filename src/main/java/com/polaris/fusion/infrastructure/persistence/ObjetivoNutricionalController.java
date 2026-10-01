package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreateObjetivoNutricionalInterface;
import com.polaris.fusion.application.in.GetObjetivoNutricionalInterface;
import com.polaris.fusion.application.in.ListObjetivoNutricionalInterface;
import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.infrastructure.persistence.dto.in.ObjetivoNutricionalFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ObjetivoNutricionalRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ObjetivoNutricionalFormDto;
import com.polaris.fusion.infrastructure.persistence.mapper.ObjetivoNutricionalFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ObjetivoNutricionalRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Inyecta las interfaces de caso de uso, no el Service. Son datos personales:
 * todo va contra usuarioActual. Sin PUT ni DELETE: el historico es inmutable.
 *
 * <p>{@code GET /api/fusion/objetivo} devuelve el vigente (con {@code ?fecha=}
 * opcional, por defecto hoy) y {@code GET /api/fusion/objetivo/historico} la
 * lista completa.
 */
@Tag(name = "Fusion - Objetivo nutricional",
     description = "Historico inmutable de objetivos diarios de kcal y macros. El vigente en un dia es el de "
             + "vigenteDesde mas reciente que no sea posterior a ese dia.")
@RestController
@RequestMapping("/api/fusion/objetivo")
@RequiredArgsConstructor
public class ObjetivoNutricionalController {

    private final CreateObjetivoNutricionalInterface createObjetivo;
    private final GetObjetivoNutricionalInterface getObjetivo;
    private final ListObjetivoNutricionalInterface listObjetivo;
    private final ObjetivoNutricionalRequestDtoMapper requestDtoMapper;
    private final ObjetivoNutricionalFormDtoMapper formDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Devuelve el objetivo vigente",
            description = "El objetivo en vigor en la fecha pedida; sin fecha, hoy.")
    @ApiResponse(responseCode = "200", description = "El objetivo vigente")
    @ApiResponse(responseCode = "400", description = "fecha no tiene el formato yyyy-MM-dd")
    @ApiResponse(responseCode = "404", description = "No tienes ningun objetivo vigente en esa fecha")
    @GetMapping
    public ResponseEntity<ObjetivoNutricionalFormDto> getVigente(@ParameterObject ObjetivoNutricionalFilterListDto filtro) {
        LocalDate fecha = filtro.fecha() != null ? filtro.fecha() : LocalDate.now();
        return ResponseEntity.ok(formDtoMapper.toFormDto(getObjetivo.getVigente(usuarioActual.id(), fecha)));
    }

    @Operation(summary = "Lista todos tus objetivos",
            description = "El historico completo, del mas reciente al mas antiguo. No admite filtros.")
    @ApiResponse(responseCode = "200", description = "Historico, vacio si nunca has fijado un objetivo")
    @GetMapping("/historico")
    public ResponseEntity<List<ObjetivoNutricionalFormDto>> list() {
        List<ObjetivoNutricional> objetivos = listObjetivo.list(usuarioActual.id());
        return ResponseEntity.ok(formDtoMapper.toFormDtoList(objetivos));
    }

    @Operation(summary = "Fija un nuevo objetivo",
            description = "No hay PUT ni DELETE: el historico no se edita, cada cambio es una fila nueva. "
                    + "vigenteDesde puede ser futura. Las kcal no tienen que cuadrar con los macros.")
    @ApiResponse(responseCode = "201", description = "Objetivo creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "409", description = "Ya tienes un objetivo con vigencia desde esa fecha")
    @PostMapping
    public ResponseEntity<ObjetivoNutricionalFormDto> create(@Valid @RequestBody ObjetivoNutricionalRequestDto dto) {
        ObjetivoNutricional creado = createObjetivo.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }
}
