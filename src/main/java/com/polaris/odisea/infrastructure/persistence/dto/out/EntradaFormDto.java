package com.polaris.odisea.infrastructure.persistence.dto.out;

import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.TipoContenido;

import java.time.LocalDate;

/**
 * La ficha completa que devuelve el detalle.
 */
public record EntradaFormDto(
        Long id,
        Long tituloId,
        String tituloTitulo,
        String tituloImagenUrl,
        TipoContenido tituloTipo,
        EstadoEntrada estado,
        Integer valoracion,
        String notas,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean favorito,
        Integer progreso
) {
}
