package com.polaris.odisea.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en TituloEntity.
 *
 * <p>Es catalogo, no lleva usuarioId: la ficha de una pelicula es la misma
 * para cualquiera. Tu relacion con ella vive en Entrada.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Titulo {

    private Long id;
    private TipoContenido tipo;
    private String titulo;
    private String tituloOriginal;
    private Integer anio;
    private String sinopsis;
    private String imagenUrl;
    private String generos;
    /** Minutos en una pelicula o serie; paginas en un libro. */
    private Integer duracionMin;
    private FuenteExterna fuenteExterna;
    private String idExterno;
}
