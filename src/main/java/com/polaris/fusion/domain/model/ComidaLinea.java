package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Una linea de una Comida: que alimento y cuantos gramos. Modelo puro.
 *
 * <p>{@code alimentoId} es el FK y siempre esta presente. {@code alimento} es la
 * ficha completa, enriquecida solo en lecturas (ComidaJpaAdapter la rellena al
 * mapear desde la Entity); al crear o actualizar llega nula. Los macros no se
 * guardan: {@link #getMacros()} los calcula, y solo se puede llamar con el
 * alimento cargado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComidaLinea {

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
