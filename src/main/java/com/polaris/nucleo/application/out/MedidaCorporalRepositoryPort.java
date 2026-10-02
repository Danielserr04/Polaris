package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.domain.model.MedidaCorporalFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de MedidaCorporal, nunca de MedidaCorporalEntity.
 */
public interface MedidaCorporalRepositoryPort {

    MedidaCorporal save(MedidaCorporal registro);

    Optional<MedidaCorporal> findById(Long id);

    /** Mas reciente primero. */
    List<MedidaCorporal> findAll(Long usuarioId, MedidaCorporalFilter filter);

    void deleteById(Long id);

    /** Para aplicar "una medicion por dia": el unique (usuario_id, fecha) de la tabla. */
    Optional<MedidaCorporal> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);
}
