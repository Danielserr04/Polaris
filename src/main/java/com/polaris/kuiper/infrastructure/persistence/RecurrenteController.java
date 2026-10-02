package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateRecurrenteInterface;
import com.polaris.kuiper.application.in.DeleteRecurrenteInterface;
import com.polaris.kuiper.application.in.GetRecurrenteInterface;
import com.polaris.kuiper.application.in.ListRecurrenteInterface;
import com.polaris.kuiper.application.in.UpdateRecurrenteInterface;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.infrastructure.persistence.dto.in.RecurrenteFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.RecurrenteRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.RecurrenteFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.RecurrenteListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteRequestDtoMapper;
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
 * Inyecta las interfaces de caso de uso, no el Service. Los cargos no se
 * generan desde aqui sino desde RecurrenteJob.
 */
@Tag(name = "Kuiper - Recurrente",
     description = "Suscripciones, recibos y pagos a plazos. Cada cargo se convierte solo en un movimiento.")
@RestController
@RequestMapping("/api/kuiper/recurrente")
@RequiredArgsConstructor
public class RecurrenteController {

    private final CreateRecurrenteInterface createRecurrente;
    private final GetRecurrenteInterface getRecurrente;
    private final ListRecurrenteInterface listRecurrente;
    private final UpdateRecurrenteInterface updateRecurrente;
    private final DeleteRecurrenteInterface deleteRecurrente;
    private final RecurrenteRequestDtoMapper requestDtoMapper;
    private final RecurrenteFilterMapper filterMapper;
    private final RecurrenteFormDtoMapper formDtoMapper;
    private final RecurrenteListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus recurrentes",
            description = "Del proximo cargo mas cercano al mas lejano. Con activo=true son los proximos cargos.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400", description = "Algun filtro no tiene un formato valido")
    @GetMapping
    public ResponseEntity<List<RecurrenteListDto>> list(@ParameterObject RecurrenteFilterListDto filtro) {
        List<Recurrente> recurrentes = listRecurrente.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(recurrentes));
    }

    @Parameter(name = "id", description = "Id del recurrente", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un recurrente")
    @ApiResponse(responseCode = "200", description = "El recurrente")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<RecurrenteFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getRecurrente.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea un recurrente",
            description = "El primer cargo es fechaInicio. Si es pasada, los cargos atrasados se generan en la "
                    + "siguiente pasada del job.")
    @ApiResponse(responseCode = "201", description = "Recurrente creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos, o el tipo no coincide con el de la categoria")
    @ApiResponse(responseCode = "404", description = "La categoria no existe o es de otro usuario")
    @PostMapping
    public ResponseEntity<RecurrenteFormDto> create(@Valid @RequestBody RecurrenteRequestDto dto) {
        Recurrente creado = createRecurrente.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @Parameter(name = "id", description = "Id del recurrente", in = ParameterIn.PATH)
    @Operation(summary = "Edita, pausa o reactiva un recurrente",
            description = "Reemplaza todos los campos. Reactivar no cobra el tiempo en pausa: el proximo "
                    + "cargo pasa a ser el primero desde hoy.")
    @ApiResponse(responseCode = "200", description = "Recurrente actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos, o el tipo no coincide con el de la categoria")
    @ApiResponse(responseCode = "404", description = "El recurrente o la categoria no existen, o son de otro usuario")
    @PutMapping("/{id}")
    public ResponseEntity<RecurrenteFormDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody RecurrenteRequestDto dto) {
        Recurrente actualizado = updateRecurrente.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @Parameter(name = "id", description = "Id del recurrente", in = ParameterIn.PATH)
    @Operation(summary = "Borra un recurrente",
            description = "Los movimientos que ya genero se quedan.")
    @ApiResponse(responseCode = "204", description = "Recurrente borrado")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRecurrente.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
