package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Gasto por categoria en el rango pedido y en el anterior equivalente.
 */
public record ComparativaCategoriasDto(
        LocalDate desde,
        LocalDate hasta,
        @Schema(description = "Primer dia del rango con el que se compara")
        LocalDate anteriorDesde,
        @Schema(description = "Ultimo dia del rango con el que se compara: el dia antes de desde")
        LocalDate anteriorHasta,
        BigDecimal total,
        BigDecimal totalAnterior,
        List<CategoriaComparadaDto> categorias
) {
}
