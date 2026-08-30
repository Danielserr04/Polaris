package com.polaris.odisea.application.out;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.domain.model.TituloFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Titulo, nunca de TituloEntity.
 */
public interface TituloRepositoryPort {

    Titulo save(Titulo titulo);

    Optional<Titulo> findById(Long id);

    List<Titulo> findAll(TituloFilter filter);

    void deleteById(Long id);

    boolean existsByIdExternoAndFuenteExterna(String idExterno, FuenteExterna fuente);

    /** Al importar: si la ficha ya esta, se reutiliza en vez de duplicarla. */
    Optional<Titulo> findByIdExternoAndFuenteExterna(String idExterno, FuenteExterna fuente);
}
