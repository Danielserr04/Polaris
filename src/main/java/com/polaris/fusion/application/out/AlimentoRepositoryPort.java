package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.fusion.domain.model.FuenteAlimento;

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

    /** Al importar: si la ficha ya esta, se reutiliza en vez de duplicarla. */
    Optional<Alimento> findByFuenteExternaAndIdExterno(FuenteAlimento fuente, String idExterno);

    void deleteById(Long id);
}
