package com.polaris.fusion.domain.model;

/**
 * Hacia donde se quiere ir: el ajuste en kcal sobre el gasto total diario.
 */
public enum TipoObjetivo {
    DEFINICION(-500),
    MANTENIMIENTO(0),
    VOLUMEN(300);

    private final int ajusteKcal;

    TipoObjetivo(int ajusteKcal) {
        this.ajusteKcal = ajusteKcal;
    }

    public int getAjusteKcal() {
        return ajusteKcal;
    }
}
