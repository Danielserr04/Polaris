package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateSesionInterface;
import com.polaris.atlas.application.in.DeleteSesionInterface;
import com.polaris.atlas.application.in.GetSesionInterface;
import com.polaris.atlas.application.in.ListSesionInterface;
import com.polaris.atlas.application.in.UpdateSesionInterface;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionListDto;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionFormDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionListDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionRequestDtoMapper;
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
 * serie sueltos: las series viajan anidadas en la sesion.
 */
@RestController
@RequestMapping("/api/atlas/sesion")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class SesionController {

    private final CreateSesionInterface createSesion;
    private final GetSesionInterface getSesion;
    private final ListSesionInterface listSesion;
    private final UpdateSesionInterface updateSesion;
    private final DeleteSesionInterface deleteSesion;
    private final SesionRequestDtoMapper requestDtoMapper;
    private final SesionFilterMapper filterMapper;
    private final SesionFormDtoMapper formDtoMapper;
    private final SesionListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<SesionListDto>> list(SesionFilterListDto filtro) {
        List<Sesion> sesiones = listSesion.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(sesiones));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SesionFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getSesion.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<SesionFormDto> create(@Valid @RequestBody SesionRequestDto dto) {
        Sesion creada = createSesion.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SesionFormDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody SesionRequestDto dto) {
        Sesion actualizada = updateSesion.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteSesion.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
