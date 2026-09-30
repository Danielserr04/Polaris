package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateRutinaInterface;
import com.polaris.atlas.application.in.DeleteRutinaInterface;
import com.polaris.atlas.application.in.GetRutinaInterface;
import com.polaris.atlas.application.in.ListRutinaInterface;
import com.polaris.atlas.application.in.UpdateRutinaInterface;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaListDto;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaFormDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaListDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaRequestDtoMapper;
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
 * linea sueltos: las lineas viajan anidadas en la rutina.
 */
@RestController
@RequestMapping("/api/atlas/rutina")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class RutinaController {

    private final CreateRutinaInterface createRutina;
    private final GetRutinaInterface getRutina;
    private final ListRutinaInterface listRutina;
    private final UpdateRutinaInterface updateRutina;
    private final DeleteRutinaInterface deleteRutina;
    private final RutinaRequestDtoMapper requestDtoMapper;
    private final RutinaFilterMapper filterMapper;
    private final RutinaFormDtoMapper formDtoMapper;
    private final RutinaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<RutinaListDto>> list(RutinaFilterListDto filtro) {
        List<Rutina> rutinas = listRutina.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(rutinas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutinaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getRutina.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<RutinaFormDto> create(@Valid @RequestBody RutinaRequestDto dto) {
        Rutina creada = createRutina.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutinaFormDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody RutinaRequestDto dto) {
        Rutina actualizada = updateRutina.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRutina.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
