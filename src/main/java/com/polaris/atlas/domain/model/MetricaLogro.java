package com.polaris.atlas.domain.model;

/**
 * Lo que mide un logro. Todas salen de datos que Atlas ya tiene: sesiones,
 * series y el peso corporal a traves de su puerto.
 */
public enum MetricaLogro {
    /** Sesiones registradas. */
    SESIONES,
    /** Ejercicios distintos con alguna serie. */
    EJERCICIOS,
    /** Volumen total levantado, en toneladas enteras (reps por peso / 1000). */
    TONELADAS,
    /** Dias con peso apuntado. */
    PESAJES,
    /** La racha mas larga de semanas seguidas con al menos una sesion. */
    RACHA_SEMANAS
}
