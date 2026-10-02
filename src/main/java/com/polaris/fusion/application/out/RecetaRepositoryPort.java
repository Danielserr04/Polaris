package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.domain.model.RecetaFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Receta (con sus ingredientes), nunca de RecetaEntity. Todo lo que
 * devuelve trae los ingredientes y sus alimentos ya cargados.
 */
public interface RecetaRepositoryPort {

    /** Guarda la receta y REEMPLAZA su conjunto de ingredientes por el que trae. */
    Receta save(Receta receta);

    Optional<Receta> findById(Long id);

    /** Por nombre, luego id. */
    List<Receta> findAll(Long usuarioId, RecetaFilter filter);

    /** Borra la receta y, en cascada, sus ingredientes. */
    void deleteById(Long id);
}
