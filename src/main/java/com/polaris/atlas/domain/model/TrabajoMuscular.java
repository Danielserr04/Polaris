package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lo que se ha trabajado un grupo muscular en un rango de fechas. No es una
 * entidad ni se guarda: se calcula sobre las series. Alimenta el mapa
 * muscular. Ver docs/decisiones/042-calendario-y-mapa-muscular.md.
 *
 * <p>{@code grupoMuscular} es el texto libre del ejercicio tal cual; agruparlo
 * en musculos del dibujo es cosa del frontend.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoMuscular {

    private String grupoMuscular;
    private int numeroSeries;
    private int numeroSesiones;
    /** Suma de reps por peso, escala 2; 0 con el peso corporal. */
    private BigDecimal volumen;
}
