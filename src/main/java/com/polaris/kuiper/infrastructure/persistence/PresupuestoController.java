package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreatePresupuestoInterface;
import com.polaris.kuiper.application.in.DeletePresupuestoInterface;
import com.polaris.kuiper.application.in.GetPresupuestoInterface;
import com.polaris.kuiper.application.in.ListPresupuestoInterface;
import com.polaris.kuiper.application.in.UpdatePresupuestoInterface;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.PresupuestoFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.PresupuestoRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.PresupuestoFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.PresupuestoListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.PresupuestoFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.PresupuestoFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.PresupuestoListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.PresupuestoRequestDtoMapper;
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
@RequestMapping("/api/kuiper/presupuesto")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PresupuestoController {

    private final CreatePresupuestoInterface createPresupuesto;
    private final GetPresupuestoInterface getPresupuesto;
    private final ListPresupuestoInterface listPresupuesto;
    private final UpdatePresupuestoInterface updatePresupuesto;
    private final DeletePresupuestoInterface deletePresupuesto;
    private final PresupuestoRequestDtoMapper requestDtoMapper;
    private final PresupuestoFilterMapper filterMapper;
    private final PresupuestoFormDtoMapper formDtoMapper;
    private final PresupuestoListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<PresupuestoListDto>> list(PresupuestoFilterListDto filtro) {
        List<Presupuesto> presupuestos = listPresupuesto.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(presupuestos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getPresupuesto.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<PresupuestoFormDto> create(@Valid @RequestBody PresupuestoRequestDto dto) {
        Presupuesto creado = createPresupuesto.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoFormDto> update(@PathVariable Long id,
                                                      @Valid @RequestBody PresupuestoRequestDto dto) {
        Presupuesto actualizado = updatePresupuesto.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePresupuesto.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
