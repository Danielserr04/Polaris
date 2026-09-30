package com.polaris.atlas.application.out;

/**
 * Lo que EjercicioService necesita saber de las series registradas. Puerto
 * propio (no se mezcla con SesionRepositoryPort): las series no se leen ni se
 * escriben sueltas, solo dentro de su Sesion.
 */
public interface SerieRegistroRepositoryPort {

    /** De CUALQUIER usuario: un ejercicio con series registradas no se puede borrar. */
    boolean existsByEjercicioId(Long ejercicioId);
}
