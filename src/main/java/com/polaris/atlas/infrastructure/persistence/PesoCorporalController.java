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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RestController("atlasPesoCorporalController")
@RequestMapping("/api/atlas/peso")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PesoCorporalController {

    private final ListPesoCorporalInterface listPesoCorporal;
    private final CreatePesoCorporalInterface createPesoCorporal;
    private final PesoCorporalRequestDtoMapper requestDtoMapper;
    private final PesoCorporalFilterMapper filterMapper;
    private final PesoCorporalDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<PesoCorporalDto>> list(PesoCorporalFilterListDto filtro) {
        List<PesoCorporal> pesos = listPesoCorporal.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(dtoMapper.toDtoList(pesos));
    }

    @PostMapping
    public ResponseEntity<PesoCorporalDto> create(@Valid @RequestBody PesoCorporalRequestDto dto) {
        PesoCorporal apuntado = createPesoCorporal.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toDto(apuntado));
    }
}
