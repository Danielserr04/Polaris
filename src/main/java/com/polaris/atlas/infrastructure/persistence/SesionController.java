package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateSesionInterface;
import com.polaris.atlas.application.in.DeleteSesionInterface;
import com.polaris.atlas.application.in.GetSesionInterface;
import com.polaris.atlas.application.in.ListSesionInterface;
import com.polaris.atlas.application.in.UpdateSesionInterface;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionListDto;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionFormDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionListDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionRequestDtoMapper;
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
 * serie sueltos: las series viajan anidadas en la sesion.
 */
@Tag(name = "Atlas - Sesion",
     description = "Entrenos registrados con sus series. Las series viajan dentro de la sesion.")
@RestController
@RequestMapping("/api/atlas/sesion")
@RequiredArgsConstructor
public class SesionController {

    private final CreateSesionInterface createSesion;
    private final GetSesionInterface getSesion;
    private final ListSesionInterface listSesion;
    private final UpdateSesionInterface updateSesion;
    private final DeleteSesionInterface deleteSesion;
    private final SesionRequestDtoMapper requestDtoMapper;
    private final SesionFilterMapper filterMapper;
    private final SesionFormDtoMapper formDtoMapper;
    private final SesionListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus sesiones",
            description = "De la mas reciente a la mas antigua, sin series y con el numero de series. Filtros "
                    + "opcionales: rango de fechas inclusivo y rutina.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<SesionListDto>> list(@ParameterObject SesionFilterListDto filtro) {
        List<Sesion> sesiones = listSesion.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(sesiones));
    }

    @Parameter(name = "id", description = "Id de la sesion", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una sesion con sus series",
            description = "Las series salen agrupadas por ejercicio, en el orden en que se registraron, y por "
                    + "numeroSerie dentro de cada ejercicio.")
    @ApiResponse(responseCode = "200", description = "La sesion")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<SesionFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getSesion.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Registra una sesion con sus series",
            description = "rutinaId es opcional: un entreno libre es valido. De 1 a 200 series; numeroSerie no "
                    + "se repite dentro de cada ejercicio. Los ejercicios tienen que ser del catalogo o "
                    + "tuyos y la rutina, tuya (activa o no).")
    @ApiResponse(responseCode = "201", description = "Sesion creada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: fecha futura, sin series o mas de 200, rangos de reps, peso o RPE, "
                    + "numero de serie repetido, o algun ejercicio o la rutina no existe o no esta "
                    + "disponible para ti")
    @PostMapping
    public ResponseEntity<SesionFormDto> create(@Valid @RequestBody SesionRequestDto dto) {
        Sesion creada = createSesion.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la sesion", in = ParameterIn.PATH)
    @Operation(summary = "Edita una sesion",
            description = "Reemplazo completo: las series que llegan sustituyen a las anteriores.")
    @ApiResponse(responseCode = "200", description = "Sesion actualizada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: fecha futura, sin series o mas de 200, rangos de reps, peso o RPE, "
                    + "numero de serie repetido, o algun ejercicio o la rutina no existe o no esta "
                    + "disponible para ti")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @PutMapping("/{id}")
    public ResponseEntity<SesionFormDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody SesionRequestDto dto) {
        Sesion actualizada = updateSesion.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la sesion", in = ParameterIn.PATH)
    @Operation(summary = "Borra una sesion",
            description = "Se borra con sus series.")
    @ApiResponse(responseCode = "204", description = "Sesion borrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteSesion.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
