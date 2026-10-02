package com.polaris.kuiper.domain.model;

/** Lo que mide un logro de Kuiper: apuntes, constancia, ahorro, metas y presupuestos. */
public enum MetricaLogro {
    /** Movimientos fuera de la papelera. */
    MOVIMIENTOS,
    /** La racha mas larga de meses seguidos con algun movimiento. */
    RACHA_MESES,
    /** Meses ya cerrados en los que entro mas de lo que salio. */
    MESES_EN_VERDE,
    /** Aportaciones a metas de ahorro. */
    APORTACIONES,
    /** Metas de ahorro cuyas aportaciones llegan al objetivo. */
    METAS_CONSEGUIDAS,
    /** Presupuestos creados. No tienen fecha. */
    PRESUPUESTOS
}
