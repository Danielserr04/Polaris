package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.domain.model.MetaEntrenoFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio. Solo
 * guarda la definicion de la meta: el valor actual y el progreso los calcula
 * el servicio al leer.
 */
public interface MetaEntrenoRepositoryPort {

    MetaEntreno save(MetaEntreno meta);

    /** Sin filtrar por usuario: quien decide si es visible es el servicio. */
    Optional<MetaEntreno> findById(Long id);

    /** Solo las del usuario, de la mas reciente a la mas antigua. */
    List<MetaEntreno> findAll(Long usuarioId, MetaEntrenoFilter filter);

    void deleteById(Long id);
}
