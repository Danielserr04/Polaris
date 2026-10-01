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
@Tag(name = "Kuiper - Presupuesto",
     description = "Limite de gasto por categoria y periodo (MENSUAL o ANUAL). Solo sobre categorias de gasto, "
             + "y uno por categoria y periodo.")
@RestController
@RequestMapping("/api/kuiper/presupuesto")
@RequiredArgsConstructor
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

    @Operation(summary = "Lista tus presupuestos",
            description = "Ordenados por nombre de categoria. Filtros opcionales por periodo y categoria.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<PresupuestoListDto>> list(@ParameterObject PresupuestoFilterListDto filtro) {
        List<Presupuesto> presupuestos = listPresupuesto.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(presupuestos));
    }

    @Parameter(name = "id", description = "Id del presupuesto", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un presupuesto",
            description = "Incluye el nombre, color e icono de la categoria.")
    @ApiResponse(responseCode = "200", description = "El presupuesto")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getPresupuesto.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea un presupuesto",
            description = "Solo sobre categorias de gasto, y uno por categoria y periodo (MENSUAL o ANUAL).")
    @ApiResponse(responseCode = "201", description = "Presupuesto creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos, o la categoria no es de gasto")
    @ApiResponse(responseCode = "404", description = "La categoria no existe o es de otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya tienes un presupuesto para esa categoria y periodo")
    @PostMapping
    public ResponseEntity<PresupuestoFormDto> create(@Valid @RequestBody PresupuestoRequestDto dto) {
        Presupuesto creado = createPresupuesto.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @Parameter(name = "id", description = "Id del presupuesto", in = ParameterIn.PATH)
    @Operation(summary = "Edita un presupuesto",
            description = "Reemplaza todos los campos del presupuesto.")
    @ApiResponse(responseCode = "200", description = "Presupuesto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos, o la categoria no es de gasto")
    @ApiResponse(responseCode = "404",
            description = "El presupuesto o la categoria no existen, o son de otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya tienes otro presupuesto para esa categoria y periodo")
    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoFormDto> update(@PathVariable Long id,
                                                      @Valid @RequestBody PresupuestoRequestDto dto) {
        Presupuesto actualizado = updatePresupuesto.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @Parameter(name = "id", description = "Id del presupuesto", in = ParameterIn.PATH)
    @Operation(summary = "Borra un presupuesto",
            description = "Quita el limite; la categoria y sus movimientos no cambian.")
    @ApiResponse(responseCode = "204", description = "Presupuesto borrado")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePresupuesto.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
