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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/api/fusion/catalogo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class CatalogoAlimentoController {

    private final BuscarCatalogoAlimentoInterface buscarCatalogo;
    private final ImportarAlimentoInterface importarAlimento;
    private final ResultadoCatalogoAlimentoDtoMapper resultadoDtoMapper;
    private final AlimentoFormDtoMapper formDtoMapper;

    @GetMapping("/buscar")
    public ResponseEntity<List<ResultadoCatalogoAlimentoDto>> buscar(@Valid CatalogoAlimentoBuscarDto filtro) {
        return ResponseEntity.ok(resultadoDtoMapper.toDtoList(buscarCatalogo.buscar(filtro.q())));
    }

    @PostMapping("/importar")
    public ResponseEntity<AlimentoFormDto> importar(@Valid @RequestBody ImportarAlimentoRequestDto dto) {
        ImportacionAlimento importacion = importarAlimento.importar(dto.idExterno());
        HttpStatus estado = importacion.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(formDtoMapper.toFormDto(importacion.alimento()));
    }
}
