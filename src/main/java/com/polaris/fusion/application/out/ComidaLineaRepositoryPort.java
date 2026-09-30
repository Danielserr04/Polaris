package com.polaris.fusion.application.out;

/**
 * Lo que AlimentoService necesita saber de las lineas de comida. Puerto propio
 * del modulo (no se mezcla con ComidaRepositoryPort: las lineas no se leen ni
 * se escriben sueltas, solo dentro de su Comida).
 */
public interface ComidaLineaRepositoryPort {

    /** De CUALQUIER usuario: el catalogo es compartido, borrarlo afectaria a todos. */
    boolean existsByAlimentoId(Long alimentoId);
}
