package com.polaris.odisea.domain.model;

/**
 * De donde viene la ficha de un Titulo. MANUAL para lo que se da de alta a
 * mano; el resto se rellena al importar del catalogo externo (B3).
 */
public enum FuenteExterna {
    TMDB,
    IGDB,
    GOOGLE_BOOKS,
    MANUAL
}
