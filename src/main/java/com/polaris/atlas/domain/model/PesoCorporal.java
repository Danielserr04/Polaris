package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * El peso corporal tal y como lo ve Atlas: el minimo que necesita. Modelo
 * propio, no el de Nucleo: {@code domain/} no importa clases de otro modulo.
 * Sin id ni usuarioId: un peso se identifica por su dia, y el usuario lo pone
 * el servicio a partir del JWT. Ver docs/decisiones/022-peso-corporal-desde-fusion-y-atlas.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesoCorporal {

    private LocalDate fecha;
    private BigDecimal pesoKg;
    /** Opcional. */
    private BigDecimal grasaPct;
    private String notas;
}
