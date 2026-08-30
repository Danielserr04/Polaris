package com.polaris.odisea.domain.model;

/**
 * Vocabulario del dominio, sin nada de JPA. El {@code @Enumerated} vive en
 * TituloEntity, donde toca.
 */
public enum TipoContenido {
    PELICULA,
    SERIE,
    JUEGO,
    LIBRO
}
