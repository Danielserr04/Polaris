package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateMetaEntrenoInterface;
import com.polaris.atlas.application.in.DeleteMetaEntrenoInterface;
import com.polaris.atlas.application.in.GetMetaEntrenoInterface;
import com.polaris.atlas.application.in.ListMetaEntrenoInterface;
import com.polaris.atlas.application.in.UpdateMetaEntrenoInterface;
import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.infrastructure.persistence.dto.in.MetaEntrenoFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.MetaEntrenoRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.MetaEntrenoFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.MetaEntrenoListDto;
import com.polaris.atlas.infrastructure.persistence.mapper.MetaEntrenoFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.MetaEntrenoFormDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.MetaEntrenoListDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.MetaEntrenoRequestDtoMapper;
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
 * comprueba contra usuarioActual: son datos personales. Las respuestas llevan
 * el valor actual y el progreso, calculados al leer.
 */
@Tag(name = "Atlas - Metas",
     description = "Metas personales de entreno: un peso corporal, una marca en un ejercicio o sesiones por "
             + "semana. Con el progreso calculado al leer.")
@RestController
@RequestMapping("/api/atlas/meta")
@RequiredArgsConstructor
public class MetaEntrenoController {

    private final CreateMetaEntrenoInterface createMetaEntreno;
    private final GetMetaEntrenoInterface getMetaEntreno;
    private final ListMetaEntrenoInterface listMetaEntreno;
    private final UpdateMetaEntrenoInterface updateMetaEntreno;
    private final DeleteMetaEntrenoInterface deleteMetaEntreno;
    private final MetaEntrenoRequestDtoMapper requestDtoMapper;
    private final MetaEntrenoFilterMapper filterMapper;
    private final MetaEntrenoFormDtoMapper formDtoMapper;
    private final MetaEntrenoListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus metas",
            description = "De la mas reciente a la mas antigua, con su progreso. Filtro opcional por tipo.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "El tipo no es valido")
    @GetMapping
    public ResponseEntity<List<MetaEntrenoListDto>> list(@ParameterObject MetaEntrenoFilterListDto filtro) {
        List<MetaEntreno> metas = listMetaEntreno.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(metas));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una meta",
            description = "Con el punto de partida y la fecha de creacion.")
    @ApiResponse(responseCode = "200", description = "La meta")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<MetaEntrenoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getMetaEntreno.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea una meta",
            description = "El punto de partida es el valor de hoy (ultimo peso o record del ejercicio).")
    @ApiResponse(responseCode = "201", description = "Meta creada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: falta el ejercicio de una marca, sesiones fuera de 1-7, objetivo "
                    + "fuera de rango o plazo pasado")
    @PostMapping
    public ResponseEntity<MetaEntrenoFormDto> create(@Valid @RequestBody MetaEntrenoRequestDto dto) {
        MetaEntreno creada = createMetaEntreno.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Edita una meta",
            description = "Reemplaza la meta. Si cambia lo que mide (tipo o ejercicio), el punto de partida "
                    + "vuelve a ser el valor de hoy.")
    @ApiResponse(responseCode = "200", description = "Meta actualizada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @PutMapping("/{id}")
    public ResponseEntity<MetaEntrenoFormDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody MetaEntrenoRequestDto dto) {
        MetaEntreno actualizada = updateMetaEntreno.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Borra una meta",
            description = "Borrado definitivo.")
    @ApiResponse(responseCode = "204", description = "Meta borrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMetaEntreno.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
