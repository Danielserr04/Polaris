package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.in.CreateTituloInterface;
import com.polaris.odisea.application.in.DeleteTituloInterface;
import com.polaris.odisea.application.in.GetTituloInterface;
import com.polaris.odisea.application.in.ListTituloInterface;
import com.polaris.odisea.application.in.UpdateTituloInterface;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloFilterListDto;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloRequestDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloFormDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloListDto;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloFilterMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloFormDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloListDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloRequestDtoMapper;
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
 * Inyecta las interfaces de caso de uso, no el Service. Catalogo compartido:
 * no filtra por usuarioId, a diferencia de EntradaController.
 */
@Tag(name = "Odisea - Titulo",
     description = "Catalogo compartido de fichas (peliculas, series, juegos y libros). No es personal: lo que "
             + "se crea o edita aqui lo ven todos los usuarios.")
@RestController
@RequestMapping("/api/odisea/titulo")
@RequiredArgsConstructor
public class TituloController {

    private final CreateTituloInterface createTitulo;
    private final GetTituloInterface getTitulo;
    private final ListTituloInterface listTitulo;
    private final UpdateTituloInterface updateTitulo;
    private final DeleteTituloInterface deleteTitulo;
    private final TituloRequestDtoMapper requestDtoMapper;
    private final TituloFilterMapper filterMapper;
    private final TituloFormDtoMapper formDtoMapper;
    private final TituloListDtoMapper listDtoMapper;

    @Operation(summary = "Lista los titulos del catalogo",
            description = "Filtros opcionales por tipo y por texto (sin distinguir mayusculas, en el titulo y "
                    + "el titulo original).")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<TituloListDto>> list(@ParameterObject TituloFilterListDto filtro) {
        return ResponseEntity.ok(listDtoMapper.toListDtoList(listTitulo.list(filterMapper.toFilter(filtro))));
    }

    @Parameter(name = "id", description = "Id del titulo", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un titulo",
            description = "Ficha completa, con sinopsis y generos.")
    @ApiResponse(responseCode = "200", description = "La ficha")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public ResponseEntity<TituloFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getTitulo.get(id)));
    }

    @Operation(summary = "Crea un titulo a mano",
            description = "Para fichas que no vienen de una fuente externa (fuenteExterna MANUAL). Para "
                    + "importar de TMDB, IGDB u Open Library usa /api/odisea/catalogo/importar.")
    @ApiResponse(responseCode = "201", description = "Titulo creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "409", description = "Ya existe un titulo con esa fuenteExterna e idExterno")
    @PostMapping
    public ResponseEntity<TituloFormDto> create(@Valid @RequestBody TituloRequestDto dto) {
        TituloFormDto creado = formDtoMapper.toFormDto(createTitulo.create(requestDtoMapper.toDomain(dto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Parameter(name = "id", description = "Id del titulo", in = ParameterIn.PATH)
    @Operation(summary = "Edita un titulo",
            description = "Reemplaza todos los campos de la ficha.")
    @ApiResponse(responseCode = "200", description = "Titulo actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "409", description = "Ya existe otro titulo con esa fuenteExterna e idExterno")
    @PutMapping("/{id}")
    public ResponseEntity<TituloFormDto> update(@PathVariable Long id, @Valid @RequestBody TituloRequestDto dto) {
        TituloFormDto actualizado = formDtoMapper.toFormDto(updateTitulo.update(id, requestDtoMapper.toDomain(dto)));
        return ResponseEntity.ok(actualizado);
    }

    @Parameter(name = "id", description = "Id del titulo", in = ParameterIn.PATH)
    @Operation(summary = "Borra un titulo",
            description = "Un titulo con entradas asociadas, de cualquier usuario, no se borra.")
    @ApiResponse(responseCode = "204", description = "Titulo borrado")
    @ApiResponse(responseCode = "400", description = "Tiene entradas asociadas")
    @ApiResponse(responseCode = "404", description = "No existe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteTitulo.delete(id);
        return ResponseEntity.noContent().build();
    }
}
