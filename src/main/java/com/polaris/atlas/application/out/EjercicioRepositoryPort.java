package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Ejercicio, nunca de EjercicioEntity.
 */
public interface EjercicioRepositoryPort {

    Ejercicio save(Ejercicio ejercicio);

    /** Sin filtrar por usuario: quien decide si es visible es el servicio. */
    Optional<Ejercicio> findById(Long id);

    /** Solo los visibles para el usuario (catalogo mas los suyos), ordenados por nombre. */
    List<Ejercicio> findAll(Long usuarioId, EjercicioFilter filter);

    void deleteById(Long id);

    /**
     * Los ejercicios visibles para el usuario (catalogo mas los suyos) con ese
     * nombre. Para aplicar la unicidad con un 409 legible.
     */
    List<Ejercicio> findVisiblesByNombre(Long usuarioId, String nombre);
}
