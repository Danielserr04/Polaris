package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de RegistroPeso, nunca de RegistroPesoEntity.
 */
public interface RegistroPesoRepositoryPort {

    RegistroPeso save(RegistroPeso registro);

    Optional<RegistroPeso> findById(Long id);

    /** Mas reciente primero. */
    List<RegistroPeso> findAll(Long usuarioId, RegistroPesoFilter filter);

    void deleteById(Long id);

    /** Para aplicar "un peso por dia": el unique (usuario_id, fecha) de la tabla. */
    Optional<RegistroPeso> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);
}
