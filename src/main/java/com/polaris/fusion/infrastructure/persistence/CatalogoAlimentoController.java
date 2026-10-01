package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.BuscarCatalogoAlimentoInterface;
import com.polaris.fusion.application.in.ImportarAlimentoInterface;
import com.polaris.fusion.domain.model.ImportacionAlimento;
import com.polaris.fusion.infrastructure.persistence.dto.in.CatalogoAlimentoBuscarDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ImportarAlimentoRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ResultadoCatalogoAlimentoDto;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ResultadoCatalogoAlimentoDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Busqueda en Open Food Facts e importacion al catalogo de alimentos.
 *
 * <p>Catalogo compartido: sin usuarioId. Importar responde 201 si crea la
 * ficha y 200 si ya existia (reutilizada), para que el cliente lo distinga.
 */
@Tag(name = "Fusion - Catalogo externo",
     description = "Busqueda de productos en Open Food Facts e importacion al catalogo de alimentos.")
@RestController
@RequestMapping("/api/fusion/catalogo")
@RequiredArgsConstructor
public class CatalogoAlimentoController {

    private final BuscarCatalogoAlimentoInterface buscarCatalogo;
    private final ImportarAlimentoInterface importarAlimento;
    private final ResultadoCatalogoAlimentoDtoMapper resultadoDtoMapper;
    private final AlimentoFormDtoMapper formDtoMapper;

    @Operation(summary = "Busca productos en Open Food Facts",
            description = "Cada resultado trae alimentoId si ese producto ya esta en el catalogo de Polaris y "
                    + "null si no. No guarda nada.")
    @ApiResponse(responseCode = "200", description = "Resultados, vacio si nada coincide")
    @ApiResponse(responseCode = "400", description = "Falta q, esta vacio o supera los 100 caracteres")
    @ApiResponse(responseCode = "502", description = "Open Food Facts no ha respondido correctamente")
    @GetMapping("/buscar")
    public ResponseEntity<List<ResultadoCatalogoAlimentoDto>> buscar(@Valid @ParameterObject CatalogoAlimentoBuscarDto filtro) {
        return ResponseEntity.ok(resultadoDtoMapper.toDtoList(buscarCatalogo.buscar(filtro.q())));
    }

    @Operation(summary = "Importa un producto de Open Food Facts",
            description = "idExterno es el codigo de barras. Si la ficha ya existia se reutiliza sin llamar a "
                    + "la API externa. No crea ninguna comida.")
    @ApiResponse(responseCode = "200", description = "La ficha ya estaba en el catalogo y se devuelve tal cual")
    @ApiResponse(responseCode = "201", description = "Ficha creada en el catalogo")
    @ApiResponse(responseCode = "400",
            description = "Cuerpo no valido, idExterno no numerico, o Open Food Facts no tiene ese producto")
    @ApiResponse(responseCode = "502", description = "Open Food Facts no ha respondido correctamente")
    @PostMapping("/importar")
    public ResponseEntity<AlimentoFormDto> importar(@Valid @RequestBody ImportarAlimentoRequestDto dto) {
        ImportacionAlimento importacion = importarAlimento.importar(dto.idExterno());
        HttpStatus estado = importacion.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(formDtoMapper.toFormDto(importacion.alimento()));
    }
}
