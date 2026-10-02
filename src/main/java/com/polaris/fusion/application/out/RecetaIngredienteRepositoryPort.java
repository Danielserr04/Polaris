package com.polaris.fusion.application.out;

/**
 * Lo que AlimentoService necesita saber de los ingredientes de receta. Puerto
 * propio, como ComidaLineaRepositoryPort: los ingredientes no se leen ni se
 * escriben sueltos, solo dentro de su Receta.
 */
public interface RecetaIngredienteRepositoryPort {

    /** De CUALQUIER usuario: el catalogo es compartido, borrarlo afectaria a todos. */
    boolean existsByAlimentoId(Long alimentoId);
}
