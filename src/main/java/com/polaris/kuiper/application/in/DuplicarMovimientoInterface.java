package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Movimiento;

import java.time.LocalDate;

/** Copia un movimiento propio con otra fecha; {@code fecha} nula = hoy. */
public interface DuplicarMovimientoInterface {
    Movimiento duplicar(Long usuarioId, Long id, LocalDate fecha);
}
