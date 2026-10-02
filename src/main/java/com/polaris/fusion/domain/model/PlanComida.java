package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Un plan semanal: que se come cada dia y en cada momento. Cada linea es un
 * alimento con sus gramos o una receta con sus raciones. Un solo agregado,
 * como Comida. Modelo puro. Ver docs/decisiones/051-plan-de-comidas-y-lista-de-la-compra.md.
 *
 * <p>Como mucho un plan activo por usuario: lo garantiza el servicio al activar.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanComida {

    private Long id;
    private Long usuarioId;
    private String nombre;
    private boolean activo;
    @Builder.Default
    private List<PlanComidaLinea> lineas = new ArrayList<>();

    /** Suma de todas las lineas, de todos los dias. */
    public Macros getTotales() {
        return lineas.stream()
                .map(PlanComidaLinea::getMacros)
                .reduce(Macros.CERO, Macros::plus);
    }

    /** Dias distintos con alguna linea (0 si el plan esta vacio). */
    public int getDiasConLineas() {
        return (int) lineas.stream().map(PlanComidaLinea::getDiaSemana).filter(Objects::nonNull).distinct().count();
    }

    /** Media de los dias que tienen lineas; cero si el plan esta vacio. */
    public Macros getMediaDiaria() {
        int dias = getDiasConLineas();
        return dias == 0 ? Macros.CERO : getTotales().escalar(BigDecimal.ONE, BigDecimal.valueOf(dias));
    }
}
