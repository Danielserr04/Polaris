package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.SesionFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Sesion (con sus series), nunca de SesionEntity. Todo lo que
 * devuelve trae las series, en el orden en que se guardaron, con sus
 * ejercicios ya cargados, y el nombre de la rutina si tiene.
 */
public interface SesionRepositoryPort {

    /** Guarda la sesion y REEMPLAZA su conjunto de series por el que trae. */
    Sesion save(Sesion sesion);

    /** Sin filtrar por usuario: quien decide si es visible es el servicio. */
    Optional<Sesion> findById(Long id);

    /** Solo las del usuario, de la mas reciente a la mas antigua. */
    List<Sesion> findAll(Long usuarioId, SesionFilter filter);

    /** Borra la sesion y, en cascada, sus series. */
    void deleteById(Long id);
}
