package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreateAlimentoInterface;
import com.polaris.fusion.application.in.DeleteAlimentoInterface;
import com.polaris.fusion.application.in.GetAlimentoInterface;
import com.polaris.fusion.application.in.ListAlimentoInterface;
import com.polaris.fusion.application.in.UpdateAlimentoInterface;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoListDto;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoFilterMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoListDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoRequestDtoMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Inyecta las interfaces de caso de uso, no el Service. Catalogo compartido:
 * no filtra por usuarioId, como TituloController.
 */
@RestController
@RequestMapping("/api/fusion/alimento")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AlimentoController {

    private final CreateAlimentoInterface createAlimento;
    private final GetAlimentoInterface getAlimento;
    private final ListAlimentoInterface listAlimento;
    private final UpdateAlimentoInterface updateAlimento;
    private final DeleteAlimentoInterface deleteAlimento;
    private final AlimentoRequestDtoMapper requestDtoMapper;
    private final AlimentoFilterMapper filterMapper;
    private final AlimentoFormDtoMapper formDtoMapper;
    private final AlimentoListDtoMapper listDtoMapper;

    @GetMapping
    public ResponseEntity<List<AlimentoListDto>> list(AlimentoFilterListDto filtro) {
        return ResponseEntity.ok(listDtoMapper.toListDtoList(listAlimento.list(filterMapper.toFilter(filtro))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlimentoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getAlimento.get(id)));
    }

    @PostMapping
    public ResponseEntity<AlimentoFormDto> create(@Valid @RequestBody AlimentoRequestDto dto) {
        AlimentoFormDto creado = formDtoMapper.toFormDto(createAlimento.create(requestDtoMapper.toDomain(dto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlimentoFormDto> update(@PathVariable Long id, @Valid @RequestBody AlimentoRequestDto dto) {
        AlimentoFormDto actualizado = formDtoMapper.toFormDto(updateAlimento.update(id, requestDtoMapper.toDomain(dto)));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteAlimento.delete(id);
        return ResponseEntity.noContent().build();
    }
}
