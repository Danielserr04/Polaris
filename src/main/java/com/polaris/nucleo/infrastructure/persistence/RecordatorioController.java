package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.DescartarRecordatorioInterface;
import com.polaris.nucleo.application.in.GetRecordatorioInterface;
import com.polaris.nucleo.application.in.ListRecordatorioInterface;
import com.polaris.nucleo.application.in.ListRecordatorioPendienteInterface;
import com.polaris.nucleo.application.in.UpdateRecordatorioInterface;
import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RecordatorioRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RecordatorioFormDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RecordatorioListDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RecordatorioPendienteDto;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioFormDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioListDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioPendienteDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * El tipo va en la ruta en vez del id: hay uno por usuario y tipo, y siempre
 * son los del usuario del JWT. Solo GET y PUT (que crea si no existe). Ver
 * docs/decisiones/044-recordatorios.md.
 */
@Tag(name = "Nucleo - Recordatorio",
     description = "Recordatorios del usuario autenticado: apuntar comidas, gastos, entrenar y presupuestos cerca "
             + "del limite. Hay uno por tipo.")
@RestController
@RequestMapping("/api/nucleo/recordatorio")
@RequiredArgsConstructor
public class RecordatorioController {

    private final ListRecordatorioInterface listRecordatorio;
    private final GetRecordatorioInterface getRecordatorio;
    private final UpdateRecordatorioInterface updateRecordatorio;
    private final ListRecordatorioPendienteInterface listPendientes;
    private final DescartarRecordatorioInterface descartarRecordatorio;
    private final RecordatorioPendienteDtoMapper pendienteDtoMapper;
    private final RecordatorioRequestDtoMapper requestDtoMapper;
    private final RecordatorioFormDtoMapper formDtoMapper;
    private final RecordatorioListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus recordatorios",
            description = "Siempre uno por tipo. Los que no has guardado salen con sus valores por defecto.")
    @ApiResponse(responseCode = "200", description = "Tus recordatorios")
    @GetMapping
    public ResponseEntity<List<RecordatorioListDto>> list() {
        return ResponseEntity.ok(listDtoMapper.toListDto(listRecordatorio.list(usuarioActual.id())));
    }

    @Operation(summary = "Lista los recordatorios pendientes de hoy",
            description = "Los que estan encendidos, tocan hoy, ya ha pasado su hora, no has descartado y siguen "
                    + "sin hacer (no hay comidas, gastos o sesion de hoy, o hay presupuestos en aviso).")
    @ApiResponse(responseCode = "200", description = "Lo que te queda por hacer hoy")
    @GetMapping("/pendientes")
    public ResponseEntity<List<RecordatorioPendienteDto>> pendientes() {
        return ResponseEntity.ok(pendienteDtoMapper.toDto(
                listPendientes.pendientes(usuarioActual.id(), LocalDateTime.now())));
    }

    @Operation(summary = "Da un recordatorio por hecho hoy",
            description = "Deja de salir en la campana y no llega al movil hasta manana.")
    @ApiResponse(responseCode = "204", description = "Descartado")
    @ApiResponse(responseCode = "400", description = "Tipo de recordatorio desconocido")
    @PostMapping("/{tipo}/descartar")
    public ResponseEntity<Void> descartar(@PathVariable TipoRecordatorio tipo) {
        descartarRecordatorio.descartar(usuarioActual.id(), tipo, LocalDate.now());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Devuelve un recordatorio",
            description = "Si no lo has guardado, sale con sus valores por defecto.")
    @ApiResponse(responseCode = "200", description = "El recordatorio")
    @ApiResponse(responseCode = "400", description = "Tipo de recordatorio desconocido")
    @GetMapping("/{tipo}")
    public ResponseEntity<RecordatorioFormDto> get(@PathVariable TipoRecordatorio tipo) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getRecordatorio.get(usuarioActual.id(), tipo)));
    }

    @Operation(summary = "Guarda un recordatorio",
            description = "Lo crea si no existe y lo reemplaza si existe.")
    @ApiResponse(responseCode = "200", description = "Recordatorio guardado")
    @ApiResponse(responseCode = "400",
            description = "Tipo desconocido, falta la hora o no hay ningun dia, o algun dia fuera de 1-7")
    @PutMapping("/{tipo}")
    public ResponseEntity<RecordatorioFormDto> update(@PathVariable TipoRecordatorio tipo,
                                                      @Valid @RequestBody RecordatorioRequestDto dto) {
        Recordatorio recordatorio = requestDtoMapper.toDomain(dto);
        recordatorio.setTipo(tipo);
        Recordatorio guardado = updateRecordatorio.update(usuarioActual.id(), recordatorio);
        return ResponseEntity.ok(formDtoMapper.toFormDto(guardado));
    }
}
