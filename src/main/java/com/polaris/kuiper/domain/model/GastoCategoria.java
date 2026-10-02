package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lo gastado en una categoria durante el mes, contra su presupuesto mensual si
 * lo tiene. {@code limiteMensual} y {@code restante} son nulos cuando no hay
 * presupuesto; {@code restante} es negativo si se ha excedido.
 *
 * <p>{@code porcentaje} es gastado / limite * 100 con un decimal, y junto con
 * {@code porcentajeAlerta} es nulo sin presupuesto. {@code estado} siempre
 * viene: SIN_PRESUPUESTO si no hay limite.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoCategoria {

    private Categoria categoria;
    private BigDecimal gastado;
    private BigDecimal limiteMensual;
    private BigDecimal restante;
    private BigDecimal porcentaje;
    private Integer porcentajeAlerta;
    private EstadoPresupuesto estado;
}
