package com.polaris.fusion.domain.model;

/** Lo que mide un logro de Fusion: comidas, constancia, recetas, planes y objetivo. */
public enum MetricaLogro {
    /** Comidas registradas. */
    COMIDAS,
    /** Dias distintos con alguna comida apuntada. */
    DIAS,
    /** La racha mas larga de dias seguidos con alguna comida apuntada. */
    RACHA_DIAS,
    /** Recetas creadas. No tienen fecha. */
    RECETAS,
    /** Planes de comidas creados. No tienen fecha. */
    PLANES,
    /** Haber fijado un objetivo nutricional alguna vez. */
    OBJETIVO
}
