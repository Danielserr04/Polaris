package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.AportarMetaAhorroInterface;
import com.polaris.kuiper.application.in.CreateMetaAhorroInterface;
import com.polaris.kuiper.application.in.DeleteAportacionMetaInterface;
import com.polaris.kuiper.application.in.DeleteMetaAhorroInterface;
import com.polaris.kuiper.application.in.GetMetaAhorroInterface;
import com.polaris.kuiper.application.in.ListAportacionMetaInterface;
import com.polaris.kuiper.application.in.ListMetaAhorroInterface;
import com.polaris.kuiper.application.in.UpdateMetaAhorroInterface;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.infrastructure.persistence.dto.in.AportacionMetaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MetaAhorroFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MetaAhorroRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.AportacionMetaDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MetaAhorroFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MetaAhorroListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.AportacionMetaDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.AportacionMetaRequestDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroRequestDtoMapper;
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
 * Inyecta las interfaces de caso de uso, no el Service. Las aportaciones
 * cuelgan de la meta: /api/kuiper/meta/{id}/aportacion.
 */
@Tag(name = "Kuiper - Meta de ahorro",
     description = "Metas de ahorro con su historial de aportaciones y retiradas. Lo ahorrado es la suma de las "
             + "aportaciones.")
@RestController
@RequestMapping("/api/kuiper/meta")
@RequiredArgsConstructor
public class MetaAhorroController {

    private final CreateMetaAhorroInterface createMeta;
    private final GetMetaAhorroInterface getMeta;
    private final ListMetaAhorroInterface listMeta;
    private final UpdateMetaAhorroInterface updateMeta;
    private final DeleteMetaAhorroInterface deleteMeta;
    private final AportarMetaAhorroInterface aportarMeta;
    private final ListAportacionMetaInterface listAportacion;
    private final DeleteAportacionMetaInterface deleteAportacion;
    private final MetaAhorroRequestDtoMapper requestDtoMapper;
    private final MetaAhorroFilterMapper filterMapper;
    private final MetaAhorroFormDtoMapper formDtoMapper;
    private final MetaAhorroListDtoMapper listDtoMapper;
    private final AportacionMetaRequestDtoMapper aportacionRequestDtoMapper;
    private final AportacionMetaDtoMapper aportacionDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus metas de ahorro",
            description = "Primero las que vencen antes; las que no tienen fecha, al final. Cada una con lo "
                    + "ahorrado, el porcentaje, lo que falta y el ahorro mensual necesario.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400", description = "Algun filtro no tiene un formato valido")
    @GetMapping
    public ResponseEntity<List<MetaAhorroListDto>> list(@ParameterObject MetaAhorroFilterListDto filtro) {
        List<MetaAhorro> metas = listMeta.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(metas));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una meta de ahorro")
    @ApiResponse(responseCode = "200", description = "La meta")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<MetaAhorroFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getMeta.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea una meta de ahorro", description = "Empieza con 0 ahorrado.")
    @ApiResponse(responseCode = "201", description = "Meta creada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "409", description = "Ya tienes una meta con ese nombre")
    @PostMapping
    public ResponseEntity<MetaAhorroFormDto> create(@Valid @RequestBody MetaAhorroRequestDto dto) {
        MetaAhorro creada = createMeta.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Edita una meta de ahorro",
            description = "Reemplaza nombre, objetivo, fecha limite, color e icono. Las aportaciones no cambian.")
    @ApiResponse(responseCode = "200", description = "Meta actualizada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya tienes otra meta con ese nombre")
    @PutMapping("/{id}")
    public ResponseEntity<MetaAhorroFormDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody MetaAhorroRequestDto dto) {
        MetaAhorro actualizada = updateMeta.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Borra una meta de ahorro", description = "Borra tambien su historial de aportaciones.")
    @ApiResponse(responseCode = "204", description = "Meta borrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMeta.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Aporta o retira dinero de una meta",
            description = "Importe positivo aporta, negativo retira. Sin fecha, hoy. Devuelve la meta con el "
                    + "total ya actualizado.")
    @ApiResponse(responseCode = "201", description = "Aportacion registrada; la meta actualizada")
    @ApiResponse(responseCode = "400", description = "Importe 0, fecha futura, o la retirada deja el total en negativo")
    @ApiResponse(responseCode = "404", description = "La meta no existe, o pertenece a otro usuario")
    @PostMapping("/{id}/aportacion")
    public ResponseEntity<MetaAhorroFormDto> aportar(@PathVariable Long id,
                                                     @Valid @RequestBody AportacionMetaRequestDto dto) {
        MetaAhorro meta = aportarMeta.aportar(usuarioActual.id(), id, aportacionRequestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(meta));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Operation(summary = "Historial de aportaciones de una meta", description = "De la mas reciente a la mas antigua.")
    @ApiResponse(responseCode = "200", description = "Historial, vacio si no tiene")
    @ApiResponse(responseCode = "404", description = "La meta no existe, o pertenece a otro usuario")
    @GetMapping("/{id}/aportacion")
    public ResponseEntity<List<AportacionMetaDto>> listAportaciones(@PathVariable Long id) {
        return ResponseEntity.ok(aportacionDtoMapper.toDtoList(listAportacion.listAportaciones(usuarioActual.id(), id)));
    }

    @Parameter(name = "id", description = "Id de la meta", in = ParameterIn.PATH)
    @Parameter(name = "aportacionId", description = "Id de la aportacion", in = ParameterIn.PATH)
    @Operation(summary = "Borra una aportacion del historial")
    @ApiResponse(responseCode = "204", description = "Aportacion borrada")
    @ApiResponse(responseCode = "400", description = "El total de la meta quedaria en negativo")
    @ApiResponse(responseCode = "404", description = "La meta o la aportacion no existen, o no son tuyas")
    @DeleteMapping("/{id}/aportacion/{aportacionId}")
    public ResponseEntity<Void> deleteAportacion(@PathVariable Long id, @PathVariable Long aportacionId) {
        deleteAportacion.deleteAportacion(usuarioActual.id(), id, aportacionId);
        return ResponseEntity.noContent().build();
    }
}
