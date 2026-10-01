package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.in.CreateEntradaInterface;
import com.polaris.odisea.application.in.DeleteEntradaInterface;
import com.polaris.odisea.application.in.GetEntradaInterface;
import com.polaris.odisea.application.in.ListEntradaInterface;
import com.polaris.odisea.application.in.UpdateEntradaInterface;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaFilterListDto;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaRequestDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaFormDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaListDto;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaFilterMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaFormDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaListDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaRequestDtoMapper;
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
 * Inyecta las interfaces de caso de uso, no el Service. A diferencia de
 * TituloController, todo aqui se filtra y se comprueba contra usuarioActual:
 * son datos personales.
 */
@Tag(name = "Odisea - Entrada",
     description = "Tu relacion personal con un titulo: estado, valoracion, progreso, notas y fechas.")
@RestController
@RequestMapping("/api/odisea/entrada")
@RequiredArgsConstructor
public class EntradaController {

    private final CreateEntradaInterface createEntrada;
    private final GetEntradaInterface getEntrada;
    private final ListEntradaInterface listEntrada;
    private final UpdateEntradaInterface updateEntrada;
    private final DeleteEntradaInterface deleteEntrada;
    private final EntradaRequestDtoMapper requestDtoMapper;
    private final EntradaFilterMapper filterMapper;
    private final EntradaFormDtoMapper formDtoMapper;
    private final EntradaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus entradas",
            description = "Con el titulo, tipo y caratula aplanados, sin notas. Filtros opcionales por tipo de "
                    + "contenido y estado.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<EntradaListDto>> list(@ParameterObject EntradaFilterListDto filtro) {
        List<Entrada> entradas = listEntrada.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(entradas));
    }

    @Parameter(name = "id", description = "Id de la entrada", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una entrada",
            description = "Con todos los campos, incluidas notas y fechas.")
    @ApiResponse(responseCode = "200", description = "La entrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<EntradaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getEntrada.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea una entrada",
            description = "Apunta en tu lista un titulo que ya esta en el catalogo (/api/odisea/titulo). Para "
                    + "traer uno de una fuente externa usa /api/odisea/catalogo/importar.")
    @ApiResponse(responseCode = "201", description = "Entrada creada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "El titulo (tituloId) no existe")
    @PostMapping
    public ResponseEntity<EntradaFormDto> create(@Valid @RequestBody EntradaRequestDto dto) {
        Entrada creada = createEntrada.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la entrada", in = ParameterIn.PATH)
    @Operation(summary = "Edita una entrada",
            description = "Reemplaza todos los campos de la entrada.")
    @ApiResponse(responseCode = "200", description = "Entrada actualizada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404",
            description = "La entrada no existe o es de otro usuario, o el titulo (tituloId) no existe")
    @PutMapping("/{id}")
    public ResponseEntity<EntradaFormDto> update(@PathVariable Long id, @Valid @RequestBody EntradaRequestDto dto) {
        Entrada actualizada = updateEntrada.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la entrada", in = ParameterIn.PATH)
    @Operation(summary = "Borra una entrada",
            description = "Solo borra tu entrada; el titulo sigue en el catalogo.")
    @ApiResponse(responseCode = "204", description = "Entrada borrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteEntrada.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
