package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateEjercicioInterface;
import com.polaris.atlas.application.in.DeleteEjercicioInterface;
import com.polaris.atlas.application.in.GetEjercicioInterface;
import com.polaris.atlas.application.in.ListEjercicioInterface;
import com.polaris.atlas.application.in.UpdateEjercicioInterface;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioListDto;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioFormDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioListDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioRequestDtoMapper;
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
 * Inyecta las interfaces de caso de uso, no el Service. Catalogo mas
 * propios: todo se resuelve contra usuarioActual, que decide que ve y que
 * puede tocar.
 */
@Tag(name = "Atlas - Ejercicio",
     description = "Catalogo compartido de ejercicios (solo lectura) mas tus ejercicios propios. Solo los "
             + "propios se pueden editar o borrar.")
@RestController
@RequestMapping("/api/atlas/ejercicio")
@RequiredArgsConstructor
public class EjercicioController {

    private final CreateEjercicioInterface createEjercicio;
    private final GetEjercicioInterface getEjercicio;
    private final ListEjercicioInterface listEjercicio;
    private final UpdateEjercicioInterface updateEjercicio;
    private final DeleteEjercicioInterface deleteEjercicio;
    private final EjercicioRequestDtoMapper requestDtoMapper;
    private final EjercicioFilterMapper filterMapper;
    private final EjercicioFormDtoMapper formDtoMapper;
    private final EjercicioListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista los ejercicios que ves",
            description = "El catalogo y los tuyos, ordenados por nombre. Filtros opcionales: grupo muscular "
                    + "(igualdad exacta, sin distinguir mayusculas ni tildes) y texto en el nombre.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @GetMapping
    public ResponseEntity<List<EjercicioListDto>> list(@ParameterObject EjercicioFilterListDto filtro) {
        List<Ejercicio> ejercicios = listEjercicio.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(ejercicios));
    }

    @Parameter(name = "id", description = "Id del ejercicio", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un ejercicio",
            description = "Sirve el del catalogo o uno propio; el propio de otro usuario da 404.")
    @ApiResponse(responseCode = "200", description = "El ejercicio")
    @ApiResponse(responseCode = "404", description = "No existe, o es propio de otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<EjercicioFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getEjercicio.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea un ejercicio propio",
            description = "Siempre queda como tuyo: el catalogo no se crea por la API. El nombre no puede "
                    + "coincidir con el de otro que veas, del catalogo o tuyo.")
    @ApiResponse(responseCode = "201", description = "Ejercicio creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "409", description = "Ya existe un ejercicio con ese nombre")
    @PostMapping
    public ResponseEntity<EjercicioFormDto> create(@Valid @RequestBody EjercicioRequestDto dto) {
        Ejercicio creado = createEjercicio.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @Parameter(name = "id", description = "Id del ejercicio", in = ParameterIn.PATH)
    @Operation(summary = "Edita un ejercicio propio",
            description = "Solo los propios: los del catalogo dan 403. El nombre no puede coincidir con el de "
                    + "otro que veas.")
    @ApiResponse(responseCode = "200", description = "Ejercicio actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "403", description = "Es del catalogo compartido: solo lectura")
    @ApiResponse(responseCode = "404", description = "No existe, o es propio de otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya existe otro ejercicio con ese nombre")
    @PutMapping("/{id}")
    public ResponseEntity<EjercicioFormDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody EjercicioRequestDto dto) {
        Ejercicio actualizado = updateEjercicio.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @Parameter(name = "id", description = "Id del ejercicio", in = ParameterIn.PATH)
    @Operation(summary = "Borra un ejercicio propio",
            description = "Un ejercicio que esta en alguna rutina o tiene series registradas no se borra.")
    @ApiResponse(responseCode = "204", description = "Ejercicio borrado")
    @ApiResponse(responseCode = "400", description = "Esta en alguna rutina o tiene series registradas")
    @ApiResponse(responseCode = "403", description = "Es del catalogo compartido: solo lectura")
    @ApiResponse(responseCode = "404", description = "No existe, o es propio de otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteEjercicio.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
