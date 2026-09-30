package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Alimento, nunca de AlimentoEntity.
 */
public interface AlimentoRepositoryPort {

    Alimento save(Alimento alimento);

    Optional<Alimento> findById(Long id);

    /** Ordenados por nombre. */
    List<Alimento> findAll(AlimentoFilter filter);

    void deleteById(Long id);
}
