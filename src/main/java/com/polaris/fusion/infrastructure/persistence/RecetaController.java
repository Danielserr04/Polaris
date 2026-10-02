package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreateRecetaInterface;
import com.polaris.fusion.application.in.DeleteRecetaInterface;
import com.polaris.fusion.application.in.GetRecetaInterface;
import com.polaris.fusion.application.in.ListRecetaInterface;
import com.polaris.fusion.application.in.UpdateRecetaInterface;
import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.infrastructure.persistence.dto.in.RecetaFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.RecetaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.RecetaFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.RecetaListDto;
import com.polaris.fusion.infrastructure.persistence.mapper.RecetaFilterMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.RecetaFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.RecetaListDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.RecetaRequestDtoMapper;
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
 * comprueba contra usuarioActual: las recetas son de cada usuario. Los
 * ingredientes viajan anidados en la receta.
 */
@Tag(name = "Fusion - Receta",
     description = "Platos reutilizables con sus ingredientes (alimento y gramos) y sus raciones. "
             + "Macros totales y por racion calculados al vuelo.")
@RestController
@RequestMapping("/api/fusion/receta")
@RequiredArgsConstructor
public class RecetaController {

    private final CreateRecetaInterface createReceta;
    private final GetRecetaInterface getReceta;
    private final ListRecetaInterface listReceta;
    private final UpdateRecetaInterface updateReceta;
    private final DeleteRecetaInterface deleteReceta;
    private final RecetaRequestDtoMapper requestDtoMapper;
    private final RecetaFilterMapper filterMapper;
    private final RecetaFormDtoMapper formDtoMapper;
    private final RecetaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus recetas",
            description = "Por nombre. Sin ingredientes, con los macros por racion. Filtro opcional ?q= por nombre.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @GetMapping
    public ResponseEntity<List<RecetaListDto>> list(@ParameterObject RecetaFilterListDto filtro) {
        List<Receta> recetas = listReceta.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(recetas));
    }

    @Parameter(name = "id", description = "Id de la receta", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una receta con sus ingredientes",
            description = "Cada ingrediente con sus macros, los totales y los macros por racion.")
    @ApiResponse(responseCode = "200", description = "La receta")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<RecetaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getReceta.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea una receta con sus ingredientes",
            description = "De 1 a 50 ingredientes, en gramos para la receta entera, y de 1 a 50 raciones.")
    @ApiResponse(responseCode = "201", description = "Receta creada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "Algun alimento de los ingredientes no existe")
    @PostMapping
    public ResponseEntity<RecetaFormDto> create(@Valid @RequestBody RecetaRequestDto dto) {
        Receta creada = createReceta.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la receta", in = ParameterIn.PATH)
    @Operation(summary = "Edita una receta",
            description = "Reemplazo completo: los ingredientes que llegan sustituyen a los anteriores. "
                    + "Los planes que la usan ven el cambio.")
    @ApiResponse(responseCode = "200", description = "Receta actualizada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404",
            description = "La receta no existe o es de otro usuario, o algun alimento no existe")
    @PutMapping("/{id}")
    public ResponseEntity<RecetaFormDto> update(@PathVariable Long id,
                                                @Valid @RequestBody RecetaRequestDto dto) {
        Receta actualizada = updateReceta.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la receta", in = ParameterIn.PATH)
    @Operation(summary = "Borra una receta", description = "Se borra con sus ingredientes.")
    @ApiResponse(responseCode = "204", description = "Receta borrada")
    @ApiResponse(responseCode = "400", description = "Esta en algun plan de comidas")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteReceta.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
