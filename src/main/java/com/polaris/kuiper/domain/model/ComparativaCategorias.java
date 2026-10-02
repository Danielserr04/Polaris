package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Gasto por categoria en [desde, hasta] comparado con [anteriorDesde,
 * anteriorHasta], el rango de la misma duracion justo antes. Si el rango son
 * meses enteros, el anterior tambien lo es.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparativaCategorias {

    private LocalDate desde;
    private LocalDate hasta;
    private LocalDate anteriorDesde;
    private LocalDate anteriorHasta;
    private BigDecimal total;
    private BigDecimal totalAnterior;
    /** Mayor gasto primero. Incluye las categorias que solo gastaron en el rango anterior. */
    private List<CategoriaComparada> categorias;
}
