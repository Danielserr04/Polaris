package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Comida (con sus lineas), nunca de ComidaEntity. Todo lo que
 * devuelve trae las lineas y sus alimentos ya cargados.
 */
public interface ComidaRepositoryPort {

    /** Guarda la comida y REEMPLAZA su conjunto de lineas por el que trae. */
    Comida save(Comida comida);

    Optional<Comida> findById(Long id);

    /** Fecha descendente, luego momento en orden natural del dia, luego id. */
    List<Comida> findAll(Long usuarioId, ComidaFilter filter);

    /** Borra la comida y, en cascada, sus lineas. */
    void deleteById(Long id);
}
