package com.polaris.odisea.domain.model;

/** Lo que mide un logro de Odisea. Todas salen de las entradas de tu biblioteca. */
public enum MetricaLogro {
    /** Titulos terminados, de cualquier tipo. */
    TERMINADOS,
    PELICULAS,
    SERIES,
    LIBROS,
    JUEGOS,
    /** Tipos de contenido distintos con algo terminado. */
    TIPOS,
    /** Entradas con valoracion. */
    VALORADAS
}
