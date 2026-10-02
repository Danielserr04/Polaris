package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.BorrarMovimientosInterface;
import com.polaris.kuiper.application.in.DeleteDefinitivoMovimientoInterface;
import com.polaris.kuiper.application.in.ListPapeleraMovimientoInterface;
import com.polaris.kuiper.application.in.RestaurarMovimientoInterface;
import com.polaris.kuiper.application.in.VaciarPapeleraMovimientoInterface;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoIdsRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoPapeleraDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoPapeleraDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * La papelera de movimientos, bajo la misma ruta que MovimientoController. Un
 * controller aparte para no mezclar el CRUD con la papelera. Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 */
@Tag(name = "Kuiper - Papelera de movimientos",
     description = "Lo borrado se guarda 30 dias: se puede restaurar o borrar de verdad antes.")
@RestController
@RequestMapping("/api/kuiper/movimiento")
@RequiredArgsConstructor
public class MovimientoPapeleraController {

    private final BorrarMovimientosInterface borrarMovimientos;
    private final ListPapeleraMovimientoInterface listPapelera;
    private final RestaurarMovimientoInterface restaurarMovimiento;
    private final DeleteDefinitivoMovimientoInterface deleteDefinitivo;
    private final VaciarPapeleraMovimientoInterface vaciarPapelera;
    private final MovimientoPapeleraDtoMapper papeleraDtoMapper;
    private final MovimientoFormDtoMapper formDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tu papelera",
            description = "Del borrado mas reciente al mas antiguo. Cada uno se elimina solo 30 dias despues de borradoEn.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si la papelera lo esta")
    @GetMapping("/papelera")
    public ResponseEntity<List<MovimientoPapeleraDto>> papelera() {
        return ResponseEntity.ok(papeleraDtoMapper.toPapeleraDtoList(listPapelera.listPapelera(usuarioActual.id())));
    }

    @Operation(summary = "Manda varios movimientos a la papelera",
            description = "Todos o ninguno: si algun id no existe, es de otro usuario o ya esta en la papelera, "
                    + "no se mueve ninguno.")
    @ApiResponse(responseCode = "204", description = "Movimientos en la papelera")
    @ApiResponse(responseCode = "400", description = "Lista vacia, con nulos o de mas de 500 ids")
    @ApiResponse(responseCode = "404", description = "Algun id no existe, es de otro usuario o ya esta en la papelera")
    @PostMapping("/borrar")
    public ResponseEntity<Void> borrar(@Valid @RequestBody MovimientoIdsRequestDto dto) {
        borrarMovimientos.borrar(usuarioActual.id(), dto.ids());
        return ResponseEntity.noContent().build();
    }

    @Parameter(name = "id", description = "Id del movimiento", in = ParameterIn.PATH)
    @Operation(summary = "Saca un movimiento de la papelera")
    @ApiResponse(responseCode = "200", description = "El movimiento restaurado")
    @ApiResponse(responseCode = "400", description = "Su categoria cambio de tipo mientras estaba en la papelera")
    @ApiResponse(responseCode = "404", description = "No esta en tu papelera")
    @PostMapping("/{id}/restaurar")
    public ResponseEntity<MovimientoFormDto> restaurar(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(restaurarMovimiento.restaurar(usuarioActual.id(), id)));
    }

    @Parameter(name = "id", description = "Id del movimiento", in = ParameterIn.PATH)
    @Operation(summary = "Borra de verdad un movimiento de la papelera",
            description = "No se puede deshacer. Solo vale para movimientos que ya estan en la papelera.")
    @ApiResponse(responseCode = "204", description = "Borrado")
    @ApiResponse(responseCode = "404", description = "No esta en tu papelera")
    @DeleteMapping("/{id}/definitivo")
    public ResponseEntity<Void> borrarDefinitivo(@PathVariable Long id) {
        deleteDefinitivo.deleteDefinitivo(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vacia tu papelera", description = "Borra de verdad todo lo que hay en ella.")
    @ApiResponse(responseCode = "204", description = "Papelera vacia")
    @DeleteMapping("/papelera")
    public ResponseEntity<Void> vaciar() {
        vaciarPapelera.vaciar(usuarioActual.id());
        return ResponseEntity.noContent().build();
    }
}
