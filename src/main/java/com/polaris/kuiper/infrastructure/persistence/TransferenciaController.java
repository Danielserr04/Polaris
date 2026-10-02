package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateTransferenciaInterface;
import com.polaris.kuiper.application.in.DeleteTransferenciaInterface;
import com.polaris.kuiper.application.in.GetTransferenciaInterface;
import com.polaris.kuiper.application.in.ListTransferenciaInterface;
import com.polaris.kuiper.application.in.UpdateTransferenciaInterface;
import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.infrastructure.persistence.dto.in.TransferenciaFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.TransferenciaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.TransferenciaFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.TransferenciaListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.TransferenciaFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.TransferenciaFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.TransferenciaListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.TransferenciaRequestDtoMapper;
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
@Tag(name = "Kuiper - Transferencia",
     description = "Dinero entre dos cuentas tuyas. No cuenta como ingreso ni como gasto.")
@RestController
@RequestMapping("/api/kuiper/transferencia")
@RequiredArgsConstructor
public class TransferenciaController {

    private final CreateTransferenciaInterface createTransferencia;
    private final GetTransferenciaInterface getTransferencia;
    private final ListTransferenciaInterface listTransferencia;
    private final UpdateTransferenciaInterface updateTransferencia;
    private final DeleteTransferenciaInterface deleteTransferencia;
    private final TransferenciaRequestDtoMapper requestDtoMapper;
    private final TransferenciaFilterMapper filterMapper;
    private final TransferenciaFormDtoMapper formDtoMapper;
    private final TransferenciaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus transferencias",
            description = "De la mas reciente a la mas antigua. Filtros opcionales: rango de fechas inclusivo y "
                    + "una cuenta (como origen o como destino).")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400", description = "Algun filtro no tiene un formato valido")
    @GetMapping
    public ResponseEntity<List<TransferenciaListDto>> list(@ParameterObject TransferenciaFilterListDto filtro) {
        List<Transferencia> transferencias = listTransferencia.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(transferencias));
    }

    @Parameter(name = "id", description = "Id de la transferencia", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una transferencia")
    @ApiResponse(responseCode = "200", description = "La transferencia")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<TransferenciaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getTransferencia.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Registra una transferencia",
            description = "Las dos cuentas tienen que ser tuyas y distintas.")
    @ApiResponse(responseCode = "201", description = "Transferencia creada")
    @ApiResponse(responseCode = "400", description = "Datos no validos, u origen y destino son la misma cuenta")
    @ApiResponse(responseCode = "404", description = "Alguna de las cuentas no existe o es de otro usuario")
    @PostMapping
    public ResponseEntity<TransferenciaFormDto> create(@Valid @RequestBody TransferenciaRequestDto dto) {
        Transferencia creada = createTransferencia.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la transferencia", in = ParameterIn.PATH)
    @Operation(summary = "Edita una transferencia", description = "Reemplaza todos los campos.")
    @ApiResponse(responseCode = "200", description = "Transferencia actualizada")
    @ApiResponse(responseCode = "400", description = "Datos no validos, u origen y destino son la misma cuenta")
    @ApiResponse(responseCode = "404", description = "La transferencia o alguna cuenta no existen, o son de otro usuario")
    @PutMapping("/{id}")
    public ResponseEntity<TransferenciaFormDto> update(@PathVariable Long id,
                                                        @Valid @RequestBody TransferenciaRequestDto dto) {
        Transferencia actualizada = updateTransferencia.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la transferencia", in = ParameterIn.PATH)
    @Operation(summary = "Borra una transferencia", description = "Los saldos de las dos cuentas se recalculan solos.")
    @ApiResponse(responseCode = "204", description = "Transferencia borrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteTransferencia.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
