package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un punto de la progresion: lo que se hizo de UN ejercicio en UNA sesion. No
 * es una entidad ni se guarda: se calcula. Ver
 * docs/decisiones/026-progresion-y-records-por-volumen.md.
 *
 * <p>{@code volumen} es la suma de reps por peso de sus series (0 si todas son
 * con el peso corporal). Todo BigDecimal, escala 2.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgresionSesion {

    private Long sesionId;
    private LocalDate fecha;
    private BigDecimal volumen;
    private int numeroSeries;
    /** El mayor peso de una serie de esa sesion. */
    private BigDecimal pesoMaximo;
    private long repsTotales;
}
