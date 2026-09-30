package com.polaris.atlas.application.out;

/**
 * Lo que EjercicioService necesita saber de las lineas de rutina. Puerto propio
 * (no se mezcla con RutinaRepositoryPort): las lineas no se leen ni se escriben
 * sueltas, solo dentro de su Rutina.
 */
public interface RutinaEjercicioRepositoryPort {

    /** De CUALQUIER usuario: un ejercicio usado en una rutina no se puede borrar. */
    boolean existsByEjercicioId(Long ejercicioId);
}
