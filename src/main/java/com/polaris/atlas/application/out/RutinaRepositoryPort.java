package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Rutina (con sus lineas), nunca de RutinaEntity. Todo lo que
 * devuelve trae las lineas, ordenadas por orden, y sus ejercicios ya cargados.
 */
public interface RutinaRepositoryPort {

    /** Guarda la rutina y REEMPLAZA su conjunto de lineas por el que trae. */
    Rutina save(Rutina rutina);

    /** Sin filtrar por usuario: quien decide si es visible es el servicio. */
    Optional<Rutina> findById(Long id);

    /** Solo las del usuario, ordenadas por nombre. */
    List<Rutina> findAll(Long usuarioId, RutinaFilter filter);

    /** Borra la rutina y, en cascada, sus lineas. */
    void deleteById(Long id);

    /**
     * La rutina del usuario con ese nombre, si existe. Se compara con la collation
     * de la columna: sin mayusculas ni tildes. Para aplicar la unicidad con un 409.
     */
    Optional<Rutina> findByUsuarioIdAndNombre(Long usuarioId, String nombre);
}
