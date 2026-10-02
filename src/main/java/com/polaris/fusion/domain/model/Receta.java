package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Un plato reutilizable: sus ingredientes (alimento y gramos) y para cuantas
 * raciones salen. Un solo agregado, como Comida. Modelo puro, sin anotaciones
 * de persistencia (el mapeo vive en RecetaEntity).
 *
 * <p>Los macros no se guardan: {@link #getTotales()} suma los de los
 * ingredientes y {@link #getPorRacion()} los divide entre las raciones. Ver
 * docs/decisiones/050-receta-agregado-con-ingredientes.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receta {

    private Long id;
    private Long usuarioId;
    private String nombre;
    private String descripcion;
    private Integer raciones;
    private String instrucciones;
    @Builder.Default
    private List<RecetaIngrediente> ingredientes = new ArrayList<>();

    public Macros getTotales() {
        return ingredientes.stream()
                .map(RecetaIngrediente::getMacros)
                .reduce(Macros.CERO, Macros::plus);
    }

    public Macros getPorRacion() {
        return getTotales().escalar(BigDecimal.ONE, BigDecimal.valueOf(raciones));
    }
}
