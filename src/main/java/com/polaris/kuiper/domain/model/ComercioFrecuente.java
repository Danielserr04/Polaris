package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Los gastos de un mismo concepto agrupados. {@code nombre} es la forma
 * escrita mas repetida del concepto; la agrupacion ignora espacios,
 * mayusculas y tildes.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercioFrecuente {

    private String nombre;
    private BigDecimal total;
    private int veces;
    private BigDecimal ticketMedio;
}
