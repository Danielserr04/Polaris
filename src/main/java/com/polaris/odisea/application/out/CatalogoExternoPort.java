package com.polaris.odisea.application.out;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;

import java.util.List;

/**
 * Una fuente externa de fichas. Hay un adaptador por fuente y el servicio elige
 * segun el tipo de contenido: no sabe quien le responde.
 *
 * <p>TMDB (peliculas y series) es el unico implementado. Juegos y libros estan
 * sin decidir, ver docs/modulos/odisea.md.
 */
public interface CatalogoExternoPort {

    /** De que tipos de contenido sabe esta fuente. */
    boolean soporta(TipoContenido tipo);

    FuenteExterna fuente();

    List<ResultadoCatalogo> buscar(String texto, TipoContenido tipo);

    /** La ficha completa, para guardarla al importar. */
    Titulo obtener(String idExterno, TipoContenido tipo);
}
