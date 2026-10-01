package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreateComidaInterface;
import com.polaris.fusion.application.in.DeleteComidaInterface;
import com.polaris.fusion.application.in.GetComidaInterface;
import com.polaris.fusion.application.in.ListComidaInterface;
import com.polaris.fusion.application.in.UpdateComidaInterface;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaListDto;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaFilterMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaListDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaRequestDtoMapper;
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
 * comprueba contra usuarioActual: son datos personales. No hay endpoints de
 * linea sueltos: las lineas viajan anidadas en la comida.
 */
@Tag(name = "Fusion - Comida",
     description = "Comidas del dia con sus lineas (alimento y gramos). Las lineas viajan dentro de la comida; "
             + "macros y totales se calculan al vuelo con los macros actuales de cada alimento.")
@RestController
@RequestMapping("/api/fusion/comida")
@RequiredArgsConstructor
public class ComidaController {

    private final CreateComidaInterface createComida;
    private final GetComidaInterface getComida;
    private final ListComidaInterface listComida;
    private final UpdateComidaInterface updateComida;
    private final DeleteComidaInterface deleteComida;
    private final ComidaRequestDtoMapper requestDtoMapper;
    private final ComidaFilterMapper filterMapper;
    private final ComidaFormDtoMapper formDtoMapper;
    private final ComidaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus comidas",
            description = "Del dia mas reciente al mas antiguo y, dentro de un dia, por momento. Sin lineas, "
                    + "con los totales. Filtros opcionales: dia exacto, rango de fechas inclusivo y "
                    + "momento.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<ComidaListDto>> list(@ParameterObject ComidaFilterListDto filtro) {
        List<Comida> comidas = listComida.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(comidas));
    }

    @Parameter(name = "id", description = "Id de la comida", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una comida con sus lineas",
            description = "Cada linea con sus macros, y los totales de la comida.")
    @ApiResponse(responseCode = "200", description = "La comida")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<ComidaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getComida.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Registra una comida con sus lineas",
            description = "De 1 a 50 lineas. Un dia puede tener varias comidas del mismo momento.")
    @ApiResponse(responseCode = "201", description = "Comida creada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: fecha futura, sin lineas o mas de 50, cantidad fuera de rango")
    @ApiResponse(responseCode = "404", description = "Algun alimento de las lineas no existe")
    @PostMapping
    public ResponseEntity<ComidaFormDto> create(@Valid @RequestBody ComidaRequestDto dto) {
        Comida creada = createComida.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la comida", in = ParameterIn.PATH)
    @Operation(summary = "Edita una comida",
            description = "Reemplazo completo: las lineas que llegan sustituyen a las anteriores.")
    @ApiResponse(responseCode = "200", description = "Comida actualizada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: fecha futura, sin lineas o mas de 50, cantidad fuera de rango")
    @ApiResponse(responseCode = "404",
            description = "La comida no existe o es de otro usuario, o algun alimento de las lineas no existe")
    @PutMapping("/{id}")
    public ResponseEntity<ComidaFormDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody ComidaRequestDto dto) {
        Comida actualizada = updateComida.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la comida", in = ParameterIn.PATH)
    @Operation(summary = "Borra una comida",
            description = "Se borra con sus lineas.")
    @ApiResponse(responseCode = "204", description = "Comida borrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteComida.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
