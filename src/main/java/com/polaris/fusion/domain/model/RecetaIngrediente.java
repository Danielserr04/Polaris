package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Un ingrediente de una Receta: que alimento y cuantos gramos para la receta
 * entera (no por racion). Modelo puro, igual que ComidaLinea: {@code alimento}
 * solo llega cargado en lecturas y {@link #getMacros()} lo necesita.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaIngrediente {

    private Long id;
    private Long usuarioId;
    private Long alimentoId;
    private Alimento alimento;
    private BigDecimal cantidadG;

    public Macros getMacros() {
        if (alimento == null) {
            throw new IllegalStateException("No se pueden calcular los macros sin el alimento cargado");
        }
        return Macros.de(alimento, cantidadG);
    }
}
