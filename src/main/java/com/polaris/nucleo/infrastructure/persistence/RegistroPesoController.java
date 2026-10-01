package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.CreateRegistroPesoInterface;
import com.polaris.nucleo.application.in.DeleteRegistroPesoInterface;
import com.polaris.nucleo.application.in.GetRegistroPesoInterface;
import com.polaris.nucleo.application.in.ListRegistroPesoInterface;
import com.polaris.nucleo.application.in.UpdateRegistroPesoInterface;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RegistroPesoFilterListDto;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RegistroPesoRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoFormDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoListDto;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoFilterMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoFormDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoListDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoRequestDtoMapper;
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
 * <p>El POST responde 201 incluso cuando actualiza el registro de ese dia
 * (un peso por dia): al cliente le da igual, quiere "este dia queda con este peso".
 */
@Tag(name = "Nucleo - Registro de peso",
     description = "Peso corporal diario, uno por dia. Es el mismo dato que ven Fusion (/api/fusion/peso) y "
             + "Atlas (/api/atlas/peso).")
@RestController
@RequestMapping("/api/nucleo/registro-peso")
@RequiredArgsConstructor
public class RegistroPesoController {

    private final CreateRegistroPesoInterface createRegistroPeso;
    private final GetRegistroPesoInterface getRegistroPeso;
    private final ListRegistroPesoInterface listRegistroPeso;
    private final UpdateRegistroPesoInterface updateRegistroPeso;
    private final DeleteRegistroPesoInterface deleteRegistroPeso;
    private final RegistroPesoRequestDtoMapper requestDtoMapper;
    private final RegistroPesoFilterMapper filterMapper;
    private final RegistroPesoFormDtoMapper formDtoMapper;
    private final RegistroPesoListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus registros de peso",
            description = "Del mas reciente al mas antiguo, sin las notas. Filtro opcional por rango de fechas "
                    + "inclusivo.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<RegistroPesoListDto>> list(@ParameterObject RegistroPesoFilterListDto filtro) {
        List<RegistroPeso> registros = listRegistroPeso.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(registros));
    }

    @Parameter(name = "id", description = "Id del registro de peso", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un registro de peso",
            description = "Con las notas.")
    @ApiResponse(responseCode = "200", description = "El registro")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<RegistroPesoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getRegistroPeso.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Registra el peso de un dia",
            description = "Un peso por dia: si ya hay registro en esa fecha lo reemplaza, y responde 201 "
                    + "igualmente. La fecha no puede ser futura.")
    @ApiResponse(responseCode = "201", description = "Peso registrado (o reemplazado, si ese dia ya tenia)")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: fecha futura, peso fuera de rango o con mas de 2 decimales, grasa "
                    + "fuera de 0-100")
    @PostMapping
    public ResponseEntity<RegistroPesoFormDto> create(@Valid @RequestBody RegistroPesoRequestDto dto) {
        RegistroPeso creado = createRegistroPeso.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @Parameter(name = "id", description = "Id del registro de peso", in = ParameterIn.PATH)
    @Operation(summary = "Edita un registro de peso",
            description = "Reemplaza el registro. Moverlo a una fecha que ya tiene otro registro da 409: aqui "
                    + "no se pisa el otro.")
    @ApiResponse(responseCode = "200", description = "Registro actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya hay un peso registrado en la fecha nueva")
    @PutMapping("/{id}")
    public ResponseEntity<RegistroPesoFormDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody RegistroPesoRequestDto dto) {
        RegistroPeso actualizado = updateRegistroPeso.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @Parameter(name = "id", description = "Id del registro de peso", in = ParameterIn.PATH)
    @Operation(summary = "Borra un registro de peso",
            description = "Desaparece tambien para Fusion y Atlas, que leen el mismo dato.")
    @ApiResponse(responseCode = "204", description = "Registro borrado")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRegistroPeso.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
