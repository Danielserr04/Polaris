package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateRutinaInterface;
import com.polaris.atlas.application.in.DeleteRutinaInterface;
import com.polaris.atlas.application.in.GetRutinaInterface;
import com.polaris.atlas.application.in.ListRutinaInterface;
import com.polaris.atlas.application.in.UpdateRutinaInterface;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaListDto;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaFilterMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaFormDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaListDtoMapper;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaRequestDtoMapper;
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
 * linea sueltos: las lineas viajan anidadas en la rutina.
 */
@Tag(name = "Atlas - Rutina",
     description = "Plantillas de entrenamiento con sus ejercicios. Las lineas viajan dentro de la rutina.")
@RestController
@RequestMapping("/api/atlas/rutina")
@RequiredArgsConstructor
public class RutinaController {

    private final CreateRutinaInterface createRutina;
    private final GetRutinaInterface getRutina;
    private final ListRutinaInterface listRutina;
    private final UpdateRutinaInterface updateRutina;
    private final DeleteRutinaInterface deleteRutina;
    private final RutinaRequestDtoMapper requestDtoMapper;
    private final RutinaFilterMapper filterMapper;
    private final RutinaFormDtoMapper formDtoMapper;
    private final RutinaListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus rutinas",
            description = "Ordenadas por nombre, sin lineas y con el numero de ejercicios. Filtro opcional por "
                    + "activa; sin el salen todas.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Algun filtro no tiene un formato valido (fecha yyyy-MM-dd, valor de enum o numero)")
    @GetMapping
    public ResponseEntity<List<RutinaListDto>> list(@ParameterObject RutinaFilterListDto filtro) {
        List<Rutina> rutinas = listRutina.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(rutinas));
    }

    @Parameter(name = "id", description = "Id de la rutina", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una rutina con sus lineas",
            description = "Las lineas salen ordenadas por orden.")
    @ApiResponse(responseCode = "200", description = "La rutina")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<RutinaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getRutina.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea una rutina con sus lineas",
            description = "De 1 a 50 lineas, con orden distinto en cada una. Los ejercicios tienen que ser del "
                    + "catalogo o tuyos. El nombre se guarda sin espacios sobrantes.")
    @ApiResponse(responseCode = "201", description = "Rutina creada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: sin lineas o mas de 50, orden repetido, o algun ejercicio no "
                    + "existe o no esta disponible para ti")
    @ApiResponse(responseCode = "409", description = "Ya tienes una rutina con ese nombre")
    @PostMapping
    public ResponseEntity<RutinaFormDto> create(@Valid @RequestBody RutinaRequestDto dto) {
        Rutina creada = createRutina.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la rutina", in = ParameterIn.PATH)
    @Operation(summary = "Edita una rutina",
            description = "Reemplazo completo: las lineas que llegan sustituyen a las anteriores.")
    @ApiResponse(responseCode = "200", description = "Rutina actualizada")
    @ApiResponse(responseCode = "400",
            description = "Datos no validos: sin lineas o mas de 50, orden repetido, o algun ejercicio no "
                    + "existe o no esta disponible para ti")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya tienes otra rutina con ese nombre")
    @PutMapping("/{id}")
    public ResponseEntity<RutinaFormDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody RutinaRequestDto dto) {
        Rutina actualizada = updateRutina.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la rutina", in = ParameterIn.PATH)
    @Operation(summary = "Borra una rutina",
            description = "Una rutina con sesiones registradas no se borra: para retirarla ponla con "
                    + "activa=false.")
    @ApiResponse(responseCode = "204", description = "Rutina borrada")
    @ApiResponse(responseCode = "400", description = "Tiene sesiones registradas")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRutina.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
