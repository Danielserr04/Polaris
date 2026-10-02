package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.CreateMedidaCorporalInterface;
import com.polaris.nucleo.application.in.DeleteMedidaCorporalInterface;
import com.polaris.nucleo.application.in.GetMedidaCorporalInterface;
import com.polaris.nucleo.application.in.ListMedidaCorporalInterface;
import com.polaris.nucleo.application.in.UpdateMedidaCorporalInterface;
import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.infrastructure.persistence.dto.in.MedidaCorporalFilterListDto;
import com.polaris.nucleo.infrastructure.persistence.dto.in.MedidaCorporalRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.MedidaCorporalFormDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.MedidaCorporalListDto;
import com.polaris.nucleo.infrastructure.persistence.mapper.MedidaCorporalFilterMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.MedidaCorporalFormDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.MedidaCorporalListDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.MedidaCorporalRequestDtoMapper;
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
 *
 * <p>Mismo patron que el registro de peso: el POST sobre un dia ya medido lo
 * reemplaza y responde 201.
 */
@Tag(name = "Nucleo - Medidas corporales",
     description = "Perimetros corporales en cm (cuello, pecho, cintura, cadera, brazos y muslos), una medicion "
             + "por dia.")
@RestController
@RequestMapping("/api/nucleo/medida-corporal")
@RequiredArgsConstructor
public class MedidaCorporalController {

    private final CreateMedidaCorporalInterface createMedidaCorporal;
    private final GetMedidaCorporalInterface getMedidaCorporal;
    private final ListMedidaCorporalInterface listMedidaCorporal;
    private final UpdateMedidaCorporalInterface updateMedidaCorporal;
    private final DeleteMedidaCorporalInterface deleteMedidaCorporal;
    private final MedidaCorporalRequestDtoMapper requestDtoMapper;
    private final MedidaCorporalFilterMapper filterMapper;
    private final MedidaCorporalFormDtoMapper formDtoMapper;
    private final MedidaCorporalListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus medidas corporales",
            description = "Del mas reciente al mas antiguo, sin las notas. Filtro opcional por rango de fechas "
                    + "inclusivo.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<MedidaCorporalListDto>> list(@ParameterObject MedidaCorporalFilterListDto filtro) {
        List<MedidaCorporal> registros = listMedidaCorporal.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(registros));
    }

    @Parameter(name = "id", description = "Id de la medicion", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una medicion",
            description = "Con las notas.")
    @ApiResponse(responseCode = "200", description = "El registro")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<MedidaCorporalFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getMedidaCorporal.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Registra las medidas de un dia",
            description = "Una medicion por dia: si ya hay registro en esa fecha lo reemplaza, y responde 201 "
                    + "igualmente. La fecha no puede ser futura.")
    @ApiResponse(responseCode = "201", description = "Medidas registradas (o reemplazadas, si ese dia ya tenia)")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: fecha futura, ninguna medida, o alguna fuera de rango o con mas "
                    + "de 1 decimal")
    @PostMapping
    public ResponseEntity<MedidaCorporalFormDto> create(@Valid @RequestBody MedidaCorporalRequestDto dto) {
        MedidaCorporal creado = createMedidaCorporal.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @Parameter(name = "id", description = "Id de la medicion", in = ParameterIn.PATH)
    @Operation(summary = "Edita una medicion",
            description = "Reemplaza el registro. Moverlo a una fecha que ya tiene otro registro da 409: aqui "
                    + "no se pisa el otro.")
    @ApiResponse(responseCode = "200", description = "Registro actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya hay medidas registradas en la fecha nueva")
    @PutMapping("/{id}")
    public ResponseEntity<MedidaCorporalFormDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody MedidaCorporalRequestDto dto) {
        MedidaCorporal actualizado = updateMedidaCorporal.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @Parameter(name = "id", description = "Id de la medicion", in = ParameterIn.PATH)
    @Operation(summary = "Borra una medicion",
            description = "Borrado definitivo.")
    @ApiResponse(responseCode = "204", description = "Registro borrado")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMedidaCorporal.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
