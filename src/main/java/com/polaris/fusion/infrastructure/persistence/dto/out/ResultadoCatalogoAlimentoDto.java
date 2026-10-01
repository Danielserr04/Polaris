package com.polaris.fusion.infrastructure.persistence.dto.out;

import com.polaris.fusion.domain.model.FuenteAlimento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Un resultado de la busqueda externa, todavia sin guardar. alimentoId viene
 * relleno si esa ficha ya esta en el catalogo de Polaris.
 */
public record ResultadoCatalogoAlimentoDto(
        FuenteAlimento fuenteExterna,
        String idExterno,
        String nombre,
        String marca,
        BigDecimal kcal100g,
        BigDecimal proteinas100g,
        BigDecimal carbohidratos100g,
        BigDecimal grasas100g,
        @Schema(description = "Id del alimento en el catalogo de Polaris si ya esta importado; null si no")
        Long alimentoId
) {
}
