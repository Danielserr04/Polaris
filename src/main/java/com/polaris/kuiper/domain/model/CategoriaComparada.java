package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * El gasto de una categoria en un rango frente al rango anterior equivalente.
 * {@code porcentaje} es su peso sobre el gasto total del rango.
 * {@code diferencia} es gastado menos gastadoAnterior; {@code variacion} es esa
 * diferencia en porcentaje sobre gastadoAnterior, nula si antes no hubo gasto.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaComparada {

    private Categoria categoria;
    private BigDecimal gastado;
    private BigDecimal porcentaje;
    private BigDecimal gastadoAnterior;
    private BigDecimal diferencia;
    private BigDecimal variacion;
}
