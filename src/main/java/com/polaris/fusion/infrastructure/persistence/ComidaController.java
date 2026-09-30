package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreateComidaInterface;
import com.polaris.fusion.application.in.DeleteComidaInterface;
import com.polaris.fusion.application.in.GetComidaInterface;
import com.polaris.fusion.application.in.ListComidaInterface;
import com.polaris.fusion.application.in.UpdateComidaInterface;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaListDto;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaFilterMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaListDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaRequestDtoMapper;
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
 * Inyecta las interfaces de caso de uso, no el Service. Todo se filtra y se
 * comprueba contra usuarioActual: son datos personales. No hay endpoints de
 * linea sueltos: las lineas viajan anidadas en la comida.
 */
@RestController
@RequestMapping("/api/fusion/comida")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class ComidaController {

    private final CreateComidaInterface createComida;
    private final GetComidaInterface getComida;
    private final ListComidaInterface listComida;
    private final UpdateComidaInterface updateComida;
    private final DeleteComidaInterface deleteComida;
    private final ComidaRequestDtoMapper requestDtoMapper;
    private final ComidaFilterMapper filterMapper;
    private final ComidaFormDtoMapper formDtoMapper;
    private final ComidaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<ComidaListDto>> list(ComidaFilterListDto filtro) {
        List<Comida> comidas = listComida.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(comidas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComidaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getComida.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<ComidaFormDto> create(@Valid @RequestBody ComidaRequestDto dto) {
        Comida creada = createComida.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComidaFormDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody ComidaRequestDto dto) {
        Comida actualizada = updateComida.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteComida.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
