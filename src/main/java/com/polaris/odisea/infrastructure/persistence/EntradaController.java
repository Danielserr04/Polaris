package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.in.CreateEntradaInterface;
import com.polaris.odisea.application.in.DeleteEntradaInterface;
import com.polaris.odisea.application.in.GetEntradaInterface;
import com.polaris.odisea.application.in.ListEntradaInterface;
import com.polaris.odisea.application.in.UpdateEntradaInterface;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaFilterListDto;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaRequestDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaFormDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaListDto;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaFilterMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaFormDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaListDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
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
 * Inyecta las interfaces de caso de uso, no el Service. A diferencia de
 * TituloController, todo aqui se filtra y se comprueba contra usuarioActual:
 * son datos personales.
 */
@RestController
@RequestMapping("/api/odisea/entrada")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class EntradaController {

    private final CreateEntradaInterface createEntrada;
    private final GetEntradaInterface getEntrada;
    private final ListEntradaInterface listEntrada;
    private final UpdateEntradaInterface updateEntrada;
    private final DeleteEntradaInterface deleteEntrada;
    private final EntradaRequestDtoMapper requestDtoMapper;
    private final EntradaFilterMapper filterMapper;
    private final EntradaFormDtoMapper formDtoMapper;
    private final EntradaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<EntradaListDto>> list(EntradaFilterListDto filtro) {
        List<Entrada> entradas = listEntrada.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(entradas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getEntrada.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<EntradaFormDto> create(@Valid @RequestBody EntradaRequestDto dto) {
        Entrada creada = createEntrada.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntradaFormDto> update(@PathVariable Long id, @Valid @RequestBody EntradaRequestDto dto) {
        Entrada actualizada = updateEntrada.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteEntrada.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
