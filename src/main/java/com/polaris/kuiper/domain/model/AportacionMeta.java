package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en AportacionMetaEntity.
 *
 * <p>Un movimiento de dinero dentro de una MetaAhorro. {@code importe} con
 * signo: positivo aporta, negativo retira. No es un Movimiento de Kuiper: no
 * cuenta como gasto ni como ingreso.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AportacionMeta {

    private Long id;
    private Long usuarioId;
    private Long metaId;
    private LocalDate fecha;
    private BigDecimal importe;
    private String nota;
}
