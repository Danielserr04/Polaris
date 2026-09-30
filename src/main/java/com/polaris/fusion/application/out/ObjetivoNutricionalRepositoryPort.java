package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.ObjetivoNutricional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Sin findById, update ni delete: el historico es inmutable.
 */
public interface ObjetivoNutricionalRepositoryPort {

    ObjetivoNutricional save(ObjetivoNutricional objetivo);

    /** El de mayor vigente_desde menor o igual a la fecha. */
    Optional<ObjetivoNutricional> findVigente(Long usuarioId, LocalDate fecha);

    /** Mas reciente primero (por vigente_desde). */
    List<ObjetivoNutricional> findAll(Long usuarioId);

    /** Para aplicar el unique (usuario_id, vigente_desde) con un 409 legible. */
    boolean existsByUsuarioIdAndVigenteDesde(Long usuarioId, LocalDate vigenteDesde);
}
