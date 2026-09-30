package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en PresupuestoEntity.
 *
 * <p>Solo guarda el limite. Cuanto se lleva gastado lo calcula el resumen,
 * a partir de los movimientos: nunca se guarda el total calculado.
 *
 * <p>{@code categoriaId} es el FK y siempre esta presente. {@code categoria} es
 * la ficha completa, enriquecida solo en lecturas (PresupuestoJpaAdapter la
 * rellena al mapear desde la Entity); al crear o actualizar llega nula.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Presupuesto {

    private Long id;
    private Long usuarioId;
    private Long categoriaId;
    private Categoria categoria;
    private PeriodoPresupuesto periodo;
    private BigDecimal importeLimite;
}
