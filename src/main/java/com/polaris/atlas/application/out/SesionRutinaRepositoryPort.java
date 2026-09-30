package com.polaris.atlas.application.out;

/**
 * Lo que RutinaService necesita saber de las sesiones. Puerto propio (no se
 * mezcla con SesionRepositoryPort), como SerieRegistroRepositoryPort para
 * Ejercicio.
 */
public interface SesionRutinaRepositoryPort {

    /** De CUALQUIER usuario: una rutina con sesiones registradas no se puede borrar. */
    boolean existsByRutinaId(Long rutinaId);
}
