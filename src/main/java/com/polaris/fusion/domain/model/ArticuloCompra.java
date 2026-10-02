package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Una fila de la lista de la compra de un plan: un alimento y cuantos gramos
 * hacen falta para toda la semana. No se guarda: se calcula al pedirla.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloCompra {

    private Long alimentoId;
    private String nombre;
    private String marca;
    private BigDecimal cantidadG;
}
