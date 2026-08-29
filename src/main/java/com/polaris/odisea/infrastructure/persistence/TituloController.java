package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.in.CreateTituloInterface;
import com.polaris.odisea.application.in.DeleteTituloInterface;
import com.polaris.odisea.application.in.GetTituloInterface;
import com.polaris.odisea.application.in.ListTituloInterface;
import com.polaris.odisea.application.in.UpdateTituloInterface;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloFilterListDto;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloRequestDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloFormDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloListDto;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloFilterMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloFormDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloListDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloRequestDtoMapper;
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
 * no filtra por usuarioId, a diferencia de EntradaController.
 */
@RestController
@RequestMapping("/api/odisea/titulo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class TituloController {

    private final CreateTituloInterface createTitulo;
    private final GetTituloInterface getTitulo;
    private final ListTituloInterface listTitulo;
    private final UpdateTituloInterface updateTitulo;
    private final DeleteTituloInterface deleteTitulo;
    private final TituloRequestDtoMapper requestDtoMapper;
    private final TituloFilterMapper filterMapper;
    private final TituloFormDtoMapper formDtoMapper;
    private final TituloListDtoMapper listDtoMapper;

    @GetMapping
    public ResponseEntity<List<TituloListDto>> list(TituloFilterListDto filtro) {
        return ResponseEntity.ok(listDtoMapper.toListDtoList(listTitulo.list(filterMapper.toFilter(filtro))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TituloFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getTitulo.get(id)));
    }

    @PostMapping
    public ResponseEntity<TituloFormDto> create(@Valid @RequestBody TituloRequestDto dto) {
        TituloFormDto creado = formDtoMapper.toFormDto(createTitulo.create(requestDtoMapper.toDomain(dto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TituloFormDto> update(@PathVariable Long id, @Valid @RequestBody TituloRequestDto dto) {
        TituloFormDto actualizado = formDtoMapper.toFormDto(updateTitulo.update(id, requestDtoMapper.toDomain(dto)));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteTitulo.delete(id);
        return ResponseEntity.noContent().build();
    }
}
