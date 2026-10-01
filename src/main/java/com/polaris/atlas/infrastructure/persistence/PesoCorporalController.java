package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreatePesoCorporalInterface;
import com.polaris.atlas.application.in.ListPesoCorporalInterface;
import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.infrastructure.persistence.dto.in.PesoCorporalFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.PesoCorporalRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.PesoCorporalDto;
import com.polaris.atlas.infrastructure.persistence.mapper.PesoCorporalDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.PesoCorporalFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.PesoCorporalRequestDtoMapper;
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

import java.util.List;

/**
 * El peso corporal visto desde Atlas. El dato es el de Nucleo
 * ({@code /api/nucleo/registro-peso}): lo que se apunta aqui aparece alli y
 * al reves. Sin PUT ni DELETE: el POST del dia existente lo reemplaza y borrar
 * se hace desde Nucleo. Inyecta las interfaces de caso de uso, no el Service.
 *
 * <p>El POST responde 201 incluso cuando actualiza el peso de ese dia, igual
 * que en Nucleo (docs/decisiones/010-registro-peso-un-peso-por-dia.md).
 */
@Tag(name = "Atlas - Peso corporal",
     description = "El peso corporal visto desde Atlas. Es el dato de Nucleo (/api/nucleo/registro-peso): lo "
             + "que se apunta aqui aparece alli y al reves.")
@RestController("atlasPesoCorporalController")
@RequestMapping("/api/atlas/peso")
@RequiredArgsConstructor
public class PesoCorporalController {

    private final ListPesoCorporalInterface listPesoCorporal;
    private final CreatePesoCorporalInterface createPesoCorporal;
    private final PesoCorporalRequestDtoMapper requestDtoMapper;
    private final PesoCorporalFilterMapper filterMapper;
    private final PesoCorporalDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus pesos",
            description = "Los mismos registros que /api/nucleo/registro-peso, con notas. Filtro opcional por "
                    + "rango de fechas inclusivo.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<PesoCorporalDto>> list(@ParameterObject PesoCorporalFilterListDto filtro) {
        List<PesoCorporal> pesos = listPesoCorporal.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(dtoMapper.toDtoList(pesos));
    }

    @Operation(summary = "Apunta el peso de un dia",
            description = "Un peso por dia: si ya hay registro en esa fecha lo reemplaza, y responde 201 "
                    + "igualmente. Para borrar un registro usa /api/nucleo/registro-peso.")
    @ApiResponse(responseCode = "201", description = "Peso apuntado (o reemplazado, si ese dia ya tenia)")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: fecha futura, peso fuera de rango o con mas de 2 decimales, grasa "
                    + "fuera de 0-100")
    @PostMapping
    public ResponseEntity<PesoCorporalDto> create(@Valid @RequestBody PesoCorporalRequestDto dto) {
        PesoCorporal apuntado = createPesoCorporal.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toDto(apuntado));
    }
}
