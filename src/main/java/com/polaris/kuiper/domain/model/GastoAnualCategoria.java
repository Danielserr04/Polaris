package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lo gastado en una categoria durante el anio contra su presupuesto ANUAL.
 * Solo existe para categorias con presupuesto anual, asi que {@code limite},
 * {@code porcentaje} y {@code porcentajeAlerta} siempre vienen.
 * {@code restante} es negativo si se ha excedido.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoAnualCategoria {

    private Categoria categoria;
    private BigDecimal gastado;
    private BigDecimal limite;
    private BigDecimal restante;
    private BigDecimal porcentaje;
    private Integer porcentajeAlerta;
    private EstadoPresupuesto estado;
}
