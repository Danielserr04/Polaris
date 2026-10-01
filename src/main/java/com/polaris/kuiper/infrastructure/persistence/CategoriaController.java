package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateCategoriaInterface;
import com.polaris.kuiper.application.in.DeleteCategoriaInterface;
import com.polaris.kuiper.application.in.GetCategoriaInterface;
import com.polaris.kuiper.application.in.ListCategoriaInterface;
import com.polaris.kuiper.application.in.UpdateCategoriaInterface;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CategoriaFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CategoriaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaListDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaRequestDtoMapper;
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
@Tag(name = "Kuiper - Categoria",
     description = "Categorias de ingreso o de gasto. El nombre es unico por usuario y tipo.")
@RestController
@RequestMapping("/api/kuiper/categoria")
@RequiredArgsConstructor
public class CategoriaController {

    private final CreateCategoriaInterface createCategoria;
    private final GetCategoriaInterface getCategoria;
    private final ListCategoriaInterface listCategoria;
    private final UpdateCategoriaInterface updateCategoria;
    private final DeleteCategoriaInterface deleteCategoria;
    private final CategoriaRequestDtoMapper requestDtoMapper;
    private final CategoriaFilterMapper filterMapper;
    private final CategoriaFormDtoMapper formDtoMapper;
    private final CategoriaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus categorias",
            description = "Ordenadas por nombre. Filtro opcional por tipo.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<CategoriaListDto>> list(@ParameterObject CategoriaFilterListDto filtro) {
        List<Categoria> categorias = listCategoria.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(categorias));
    }

    @Parameter(name = "id", description = "Id de la categoria", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una categoria",
            description = "Solo las tuyas: la de otro usuario da 404 como si no existiera.")
    @ApiResponse(responseCode = "200", description = "La categoria")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getCategoria.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea una categoria",
            description = "El nombre es unico por usuario y tipo: puedes tener un GASTO y un INGRESO con el "
                    + "mismo nombre.")
    @ApiResponse(responseCode = "201", description = "Categoria creada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "409", description = "Ya tienes una categoria con ese nombre y tipo")
    @PostMapping
    public ResponseEntity<CategoriaFormDto> create(@Valid @RequestBody CategoriaRequestDto dto) {
        Categoria creada = createCategoria.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la categoria", in = ParameterIn.PATH)
    @Operation(summary = "Edita una categoria",
            description = "No se puede cambiar el tipo de una categoria que tiene movimientos o presupuestos.")
    @ApiResponse(responseCode = "200", description = "Categoria actualizada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos, o cambio de tipo con movimientos o presupuestos asociados")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya tienes otra categoria con ese nombre y tipo")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaFormDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody CategoriaRequestDto dto) {
        Categoria actualizada = updateCategoria.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la categoria", in = ParameterIn.PATH)
    @Operation(summary = "Borra una categoria",
            description = "Una categoria con movimientos o presupuestos no se borra.")
    @ApiResponse(responseCode = "204", description = "Categoria borrada")
    @ApiResponse(responseCode = "400", description = "Tiene movimientos o presupuestos asociados")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteCategoria.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
