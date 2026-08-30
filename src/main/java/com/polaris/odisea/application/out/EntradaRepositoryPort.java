package com.polaris.odisea.application.out;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EntradaFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Entrada, nunca de EntradaEntity.
 */
public interface EntradaRepositoryPort {

    Entrada save(Entrada entrada);

    Optional<Entrada> findById(Long id);

    List<Entrada> findAll(Long usuarioId, EntradaFilter filter);

    void deleteById(Long id);

    /** Para que TituloService pueda impedir borrar un titulo con entradas asociadas. */
    boolean existsByTituloId(Long tituloId);

    /** Para que importar dos veces la misma ficha no duplique tu entrada. */
    boolean existsByUsuarioIdAndTituloId(Long usuarioId, Long tituloId);
}
