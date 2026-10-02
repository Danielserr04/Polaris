package com.polaris.fusion.domain.model;

import java.math.BigDecimal;

/**
 * Nivel de actividad con su factor multiplicador sobre el gasto basal (los
 * habituales de Mifflin-St Jeor). Enum propio de Fusion, con los mismos
 * valores que el del perfil de Nucleo.
 */
public enum NivelActividad {
    SEDENTARIO("1.2"),
    LIGERO("1.375"),
    MODERADO("1.55"),
    ALTO("1.725"),
    MUY_ALTO("1.9");

    private final BigDecimal factor;

    NivelActividad(String factor) {
        this.factor = new BigDecimal(factor);
    }

    public BigDecimal getFactor() {
        return factor;
    }
}
