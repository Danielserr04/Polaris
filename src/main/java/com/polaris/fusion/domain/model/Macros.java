package com.polaris.fusion.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Kcal y macros de una cantidad de comida. Nunca se persiste: se calcula al
 * vuelo desde los valores por 100 g del alimento y la cantidad de la linea.
 *
 * <p>Dominio puro (sin Spring). Cada valor = valor_100g * cantidad_g / 100,
 * con escala 2 y HALF_UP. El total de una comida es la suma de las lineas ya
 * redondeadas, para que el total siempre cuadre con lo que se ve por linea.
 * Ver docs/decisiones/017-comida-agregado-con-lineas-macros-al-vuelo.md.
 */
public record Macros(BigDecimal kcal, BigDecimal proteinas, BigDecimal carbohidratos, BigDecimal grasas) {

    private static final int ESCALA = 2;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public static final Macros CERO = new Macros(
            BigDecimal.ZERO.setScale(ESCALA), BigDecimal.ZERO.setScale(ESCALA),
            BigDecimal.ZERO.setScale(ESCALA), BigDecimal.ZERO.setScale(ESCALA));

    /** Macros de {@code cantidadG} gramos de {@code alimento}. */
    public static Macros de(Alimento alimento, BigDecimal cantidadG) {
        return new Macros(
                porCantidad(alimento.getKcal100g(), cantidadG),
                porCantidad(alimento.getProteinas100g(), cantidadG),
                porCantidad(alimento.getCarbohidratos100g(), cantidadG),
                porCantidad(alimento.getGrasas100g(), cantidadG));
    }

    public Macros plus(Macros otro) {
        return new Macros(
                kcal.add(otro.kcal),
                proteinas.add(otro.proteinas),
                carbohidratos.add(otro.carbohidratos),
                grasas.add(otro.grasas));
    }

    private static BigDecimal porCantidad(BigDecimal valor100g, BigDecimal cantidadG) {
        return valor100g.multiply(cantidadG).divide(CIEN, ESCALA, RoundingMode.HALF_UP);
    }
}
