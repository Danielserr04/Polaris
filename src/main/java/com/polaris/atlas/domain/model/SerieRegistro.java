package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Una serie concreta de una Sesion: reps, peso y esfuerzo. Modelo puro.
 *
 * <p>{@code ejercicioId} es el FK y siempre esta presente. {@code ejercicio} es
 * la ficha completa, enriquecida solo en lecturas (SesionJpaAdapter la rellena
 * al mapear desde la Entity); al crear o actualizar llega nula. El peso y el
 * RPE son BigDecimal, nunca double: se comparan y se suman sin error de
 * redondeo. {@code rpe} es opcional.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieRegistro {

    private Long id;
    private Long usuarioId;
    private Long ejercicioId;
    private Ejercicio ejercicio;
    private Integer numeroSerie;
    private Integer reps;
    private BigDecimal pesoKg;
    /** Esfuerzo percibido, de 1 a 10 en pasos de 0.5. Opcional. */
    private BigDecimal rpe;
}
