package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreateAlimentoInterface;
import com.polaris.fusion.application.in.DeleteAlimentoInterface;
import com.polaris.fusion.application.in.GetAlimentoInterface;
import com.polaris.fusion.application.in.ListAlimentoInterface;
import com.polaris.fusion.application.in.UpdateAlimentoInterface;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoListDto;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoFilterMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoListDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoRequestDtoMapper;
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
 * no filtra por usuarioId, como TituloController.
 */
@Tag(name = "Fusion - Alimento",
     description = "Catalogo compartido de alimentos, con los macros por 100 g. No es personal: lo que se crea "
             + "o edita aqui lo ven todos los usuarios.")
@RestController
@RequestMapping("/api/fusion/alimento")
@RequiredArgsConstructor
public class AlimentoController {

    private final CreateAlimentoInterface createAlimento;
    private final GetAlimentoInterface getAlimento;
    private final ListAlimentoInterface listAlimento;
    private final UpdateAlimentoInterface updateAlimento;
    private final DeleteAlimentoInterface deleteAlimento;
    private final AlimentoRequestDtoMapper requestDtoMapper;
    private final AlimentoFilterMapper filterMapper;
    private final AlimentoFormDtoMapper formDtoMapper;
    private final AlimentoListDtoMapper listDtoMapper;

    @Operation(summary = "Lista los alimentos del catalogo",
            description = "Ordenados por nombre, sin fuente ni idExterno. El filtro q busca en nombre y marca, "
                    + "sin distinguir mayusculas.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @GetMapping
    public ResponseEntity<List<AlimentoListDto>> list(@ParameterObject AlimentoFilterListDto filtro) {
        return ResponseEntity.ok(listDtoMapper.toListDtoList(listAlimento.list(filterMapper.toFilter(filtro))));
    }

    @Parameter(name = "id", description = "Id del alimento", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un alimento",
            description = "Ficha completa, con fuente e idExterno.")
    @ApiResponse(responseCode = "200", description = "La ficha")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{id}")
    public ResponseEntity<AlimentoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getAlimento.get(id)));
    }

    @Operation(summary = "Crea un alimento a mano",
            description = "Siempre queda con fuente MANUAL y sin idExterno. Los macros son por 100 g. Para "
                    + "traer un producto de Open Food Facts usa /api/fusion/catalogo/importar.")
    @ApiResponse(responseCode = "201", description = "Alimento creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @PostMapping
    public ResponseEntity<AlimentoFormDto> create(@Valid @RequestBody AlimentoRequestDto dto) {
        AlimentoFormDto creado = formDtoMapper.toFormDto(createAlimento.create(requestDtoMapper.toDomain(dto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Parameter(name = "id", description = "Id del alimento", in = ParameterIn.PATH)
    @Operation(summary = "Edita un alimento",
            description = "Conserva la fuente y el idExterno originales: se editan los datos, no el origen. "
                    + "Como los macros de las comidas se calculan al vuelo, el cambio afecta a todas las "
                    + "comidas que lo usan.")
    @ApiResponse(responseCode = "200", description = "Alimento actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "No existe")
    @PutMapping("/{id}")
    public ResponseEntity<AlimentoFormDto> update(@PathVariable Long id, @Valid @RequestBody AlimentoRequestDto dto) {
        AlimentoFormDto actualizado = formDtoMapper.toFormDto(updateAlimento.update(id, requestDtoMapper.toDomain(dto)));
        return ResponseEntity.ok(actualizado);
    }

    @Parameter(name = "id", description = "Id del alimento", in = ParameterIn.PATH)
    @Operation(summary = "Borra un alimento",
            description = "Un alimento que esta en alguna linea de comida, de cualquier usuario, no se borra.")
    @ApiResponse(responseCode = "204", description = "Alimento borrado")
    @ApiResponse(responseCode = "400", description = "Esta en alguna comida")
    @ApiResponse(responseCode = "404", description = "No existe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteAlimento.delete(id);
        return ResponseEntity.noContent().build();
    }
}
