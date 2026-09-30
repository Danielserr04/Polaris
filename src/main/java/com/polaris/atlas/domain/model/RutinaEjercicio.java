package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una linea de una Rutina: que ejercicio, en que orden y con que objetivo.
 * Modelo puro.
 *
 * <p>{@code ejercicioId} es el FK y siempre esta presente. {@code ejercicio} es
 * la ficha completa, enriquecida solo en lecturas (RutinaJpaAdapter la rellena
 * al mapear desde la Entity); al crear o actualizar llega nula.
 * {@code repsObjetivo} es texto libre: "8-12", "5", "AMRAP".
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutinaEjercicio {

    private Long id;
    private Long usuarioId;
    private Long ejercicioId;
    private Ejercicio ejercicio;
    private Integer orden;
    private Integer seriesObjetivo;
    private String repsObjetivo;
}
