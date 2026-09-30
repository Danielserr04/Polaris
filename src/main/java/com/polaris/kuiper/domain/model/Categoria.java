package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en CategoriaEntity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    private Long id;
    private Long usuarioId;
    private String nombre;
    /** Hex #RRGGBB. Opcional. */
    private String color;
    /** Nombre del icono en el frontend. Opcional. */
    private String icono;
    private TipoMovimiento tipo;
}
