package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.domain.model.PlanComidaFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de PlanComida (con sus lineas), nunca de PlanComidaEntity. Todo lo que
 * devuelve trae las lineas con su alimento o su receta (y los ingredientes de
 * esta) ya cargados.
 */
public interface PlanComidaRepositoryPort {

    /** Guarda el plan y REEMPLAZA su conjunto de lineas por el que trae. */
    PlanComida save(PlanComida plan);

    Optional<PlanComida> findById(Long id);

    /** El activo primero, luego por nombre y id. */
    List<PlanComida> findAll(Long usuarioId, PlanComidaFilter filter);

    /** Borra el plan y, en cascada, sus lineas. */
    void deleteById(Long id);

    /**
     * Deja {@code id} como el unico plan activo del usuario: en una sola
     * operacion, para que nunca queden dos activos ni ninguno a medias.
     */
    void activarUnico(Long usuarioId, Long id);
}
