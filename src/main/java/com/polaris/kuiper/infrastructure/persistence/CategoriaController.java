package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateCategoriaInterface;
import com.polaris.kuiper.application.in.DeleteCategoriaInterface;
import com.polaris.kuiper.application.in.GetCategoriaInterface;
import com.polaris.kuiper.application.in.ListCategoriaInterface;
import com.polaris.kuiper.application.in.UpdateCategoriaInterface;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CategoriaFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CategoriaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaRequestDtoMapper;
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
 * comprueba contra usuarioActual: son datos personales.
 */
@RestController
@RequestMapping("/api/kuiper/categoria")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class CategoriaController {

    private final CreateCategoriaInterface createCategoria;
    private final GetCategoriaInterface getCategoria;
    private final ListCategoriaInterface listCategoria;
    private final UpdateCategoriaInterface updateCategoria;
    private final DeleteCategoriaInterface deleteCategoria;
    private final CategoriaRequestDtoMapper requestDtoMapper;
    private final CategoriaFilterMapper filterMapper;
    private final CategoriaFormDtoMapper formDtoMapper;
    private final CategoriaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<CategoriaListDto>> list(CategoriaFilterListDto filtro) {
        List<Categoria> categorias = listCategoria.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(categorias));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getCategoria.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<CategoriaFormDto> create(@Valid @RequestBody CategoriaRequestDto dto) {
        Categoria creada = createCategoria.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaFormDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody CategoriaRequestDto dto) {
        Categoria actualizada = updateCategoria.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteCategoria.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
