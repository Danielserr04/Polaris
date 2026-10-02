package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Cuenta, nunca de CuentaEntity. No sabe nada del saldo actual: ese
 * lo calcula CuentaService con las sumas de movimientos y transferencias.
 */
public interface CuentaRepositoryPort {

    Cuenta save(Cuenta cuenta);

    Optional<Cuenta> findById(Long id);

    /** Activas primero y, dentro de cada grupo, por nombre. */
    List<Cuenta> findAll(Long usuarioId, CuentaFilter filter);

    void deleteById(Long id);

    /** Para aplicar el unique (usuario_id, nombre) con un 409 legible. */
    Optional<Cuenta> findByUsuarioIdAndNombre(Long usuarioId, String nombre);
}
