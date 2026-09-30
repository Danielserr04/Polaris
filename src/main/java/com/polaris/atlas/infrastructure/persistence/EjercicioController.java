package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateEjercicioInterface;
import com.polaris.atlas.application.in.DeleteEjercicioInterface;
import com.polaris.atlas.application.in.GetEjercicioInterface;
import com.polaris.atlas.application.in.ListEjercicioInterface;
import com.polaris.atlas.application.in.UpdateEjercicioInterface;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioListDto;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioFormDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioListDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioRequestDtoMapper;
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
 * Inyecta las interfaces de caso de uso, no el Service. Catalogo mas
 * propios: todo se resuelve contra usuarioActual, que decide que ve y que
 * puede tocar.
 */
@RestController
@RequestMapping("/api/atlas/ejercicio")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class EjercicioController {

    private final CreateEjercicioInterface createEjercicio;
    private final GetEjercicioInterface getEjercicio;
    private final ListEjercicioInterface listEjercicio;
    private final UpdateEjercicioInterface updateEjercicio;
    private final DeleteEjercicioInterface deleteEjercicio;
    private final EjercicioRequestDtoMapper requestDtoMapper;
    private final EjercicioFilterMapper filterMapper;
    private final EjercicioFormDtoMapper formDtoMapper;
    private final EjercicioListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<EjercicioListDto>> list(EjercicioFilterListDto filtro) {
        List<Ejercicio> ejercicios = listEjercicio.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(ejercicios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EjercicioFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getEjercicio.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<EjercicioFormDto> create(@Valid @RequestBody EjercicioRequestDto dto) {
        Ejercicio creado = createEjercicio.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EjercicioFormDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody EjercicioRequestDto dto) {
        Ejercicio actualizado = updateEjercicio.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteEjercicio.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
