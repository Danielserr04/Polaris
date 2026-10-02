package com.polaris.atlas.domain.model;

/** Lo que mide un logro de Atlas. Todas salen de las sesiones y sus series. */
public enum MetricaLogro {
    /** Sesiones registradas. */
    SESIONES,
    /** Ejercicios distintos con alguna serie. */
    EJERCICIOS,
    /** Volumen total levantado, en toneladas enteras (reps por peso / 1000). */
    TONELADAS,
    /** La racha mas larga de semanas seguidas con al menos una sesion. */
    RACHA_SEMANAS
}
