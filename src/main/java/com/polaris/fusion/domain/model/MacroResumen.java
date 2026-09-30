package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Un macro del dia contra su objetivo. Sin objetivo vigente, {@code objetivo},
 * {@code restante} y {@code porcentaje} van a null. {@code restante} es
 * objetivo menos consumido y puede ser negativo. {@code porcentaje} es
 * consumido sobre objetivo, en %, y es null tambien si el objetivo es 0.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacroResumen {

    private BigDecimal consumido;
    private BigDecimal objetivo;
    private BigDecimal restante;
    private BigDecimal porcentaje;
}
