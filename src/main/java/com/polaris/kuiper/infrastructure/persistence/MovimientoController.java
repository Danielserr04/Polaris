package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateMovimientoInterface;
import com.polaris.kuiper.application.in.DeleteMovimientoInterface;
import com.polaris.kuiper.application.in.GetMovimientoInterface;
import com.polaris.kuiper.application.in.ListMovimientoInterface;
import com.polaris.kuiper.application.in.UpdateMovimientoInterface;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoRequestDtoMapper;
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
@RequestMapping("/api/kuiper/movimiento")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class MovimientoController {

    private final CreateMovimientoInterface createMovimiento;
    private final GetMovimientoInterface getMovimiento;
    private final ListMovimientoInterface listMovimiento;
    private final UpdateMovimientoInterface updateMovimiento;
    private final DeleteMovimientoInterface deleteMovimiento;
    private final MovimientoRequestDtoMapper requestDtoMapper;
    private final MovimientoFilterMapper filterMapper;
    private final MovimientoFormDtoMapper formDtoMapper;
    private final MovimientoListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<MovimientoListDto>> list(MovimientoFilterListDto filtro) {
        List<Movimiento> movimientos = listMovimiento.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(movimientos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getMovimiento.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<MovimientoFormDto> create(@Valid @RequestBody MovimientoRequestDto dto) {
        Movimiento creado = createMovimiento.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoFormDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody MovimientoRequestDto dto) {
        Movimiento actualizado = updateMovimiento.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMovimiento.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
