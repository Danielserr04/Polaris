package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateMovimientoInterface;
import com.polaris.kuiper.application.in.DeleteMovimientoInterface;
import com.polaris.kuiper.application.in.DuplicarMovimientoInterface;
import com.polaris.kuiper.application.in.GetMovimientoInterface;
import com.polaris.kuiper.application.in.ListMovimientoInterface;
import com.polaris.kuiper.application.in.UpdateMovimientoInterface;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoDuplicarRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Kuiper - Movimiento",
     description = "Ingresos y gastos. El importe va siempre en positivo: el signo lo da el tipo.")
@RestController
@RequestMapping("/api/kuiper/movimiento")
@RequiredArgsConstructor
public class MovimientoController {

    private final CreateMovimientoInterface createMovimiento;
    private final GetMovimientoInterface getMovimiento;
    private final ListMovimientoInterface listMovimiento;
    private final UpdateMovimientoInterface updateMovimiento;
    private final DeleteMovimientoInterface deleteMovimiento;
    private final DuplicarMovimientoInterface duplicarMovimiento;
    private final MovimientoRequestDtoMapper requestDtoMapper;
    private final MovimientoFilterMapper filterMapper;
    private final MovimientoFormDtoMapper formDtoMapper;
    private final MovimientoListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus movimientos",
            description = "Del mas reciente al mas antiguo. Filtros opcionales: rango de fechas inclusivo, "
                    + "categoria y tipo.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<MovimientoListDto>> list(@ParameterObject MovimientoFilterListDto filtro) {
        List<Movimiento> movimientos = listMovimiento.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(movimientos));
    }

    @Parameter(name = "id", description = "Id del movimiento", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un movimiento",
            description = "Incluye el nombre, color e icono de la categoria.")
    @ApiResponse(responseCode = "200", description = "El movimiento")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getMovimiento.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Registra un movimiento",
            description = "La categoria tiene que ser tuya y del mismo tipo que el movimiento.")
    @ApiResponse(responseCode = "201", description = "Movimiento creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos, o el tipo no coincide con el de la categoria")
    @ApiResponse(responseCode = "404", description = "La categoria no existe o es de otro usuario")
    @PostMapping
    public ResponseEntity<MovimientoFormDto> create(@Valid @RequestBody MovimientoRequestDto dto) {
        Movimiento creado = createMovimiento.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @Parameter(name = "id", description = "Id del movimiento", in = ParameterIn.PATH)
    @Operation(summary = "Edita un movimiento",
            description = "Reemplaza todos los campos del movimiento.")
    @ApiResponse(responseCode = "200", description = "Movimiento actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos, o el tipo no coincide con el de la categoria")
    @ApiResponse(responseCode = "404", description = "El movimiento o la categoria no existen, o son de otro usuario")
    @PutMapping("/{id}")
    public ResponseEntity<MovimientoFormDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody MovimientoRequestDto dto) {
        Movimiento actualizado = updateMovimiento.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @Parameter(name = "id", description = "Id del movimiento", in = ParameterIn.PATH)
    @Operation(summary = "Manda un movimiento a la papelera",
            description = "Deja de salir en listados y resumenes; se puede restaurar durante 30 dias. "
                    + "No afecta a la categoria.")
    @ApiResponse(responseCode = "204", description = "Movimiento en la papelera")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMovimiento.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Parameter(name = "id", description = "Id del movimiento", in = ParameterIn.PATH)
    @Operation(summary = "Duplica un movimiento",
            description = "Copia importe, tipo, categoria, concepto y metodo de pago con la fecha del cuerpo, "
                    + "u hoy si no viene. La copia no se marca como recurrente.")
    @ApiResponse(responseCode = "201", description = "La copia")
    @ApiResponse(responseCode = "400", description = "La fecha es futura o no tiene formato yyyy-MM-dd")
    @ApiResponse(responseCode = "404", description = "No existe, esta en la papelera o pertenece a otro usuario")
    @PostMapping("/{id}/duplicar")
    public ResponseEntity<MovimientoFormDto> duplicar(@PathVariable Long id,
                                                       @Valid @RequestBody(required = false)
                                                       MovimientoDuplicarRequestDto dto) {
        Movimiento copia = duplicarMovimiento.duplicar(usuarioActual.id(), id, dto == null ? null : dto.fecha());
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(copia));
    }
}
