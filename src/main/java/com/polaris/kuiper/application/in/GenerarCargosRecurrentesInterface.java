package com.polaris.kuiper.application.in;

import java.time.LocalDate;

/**
 * Lo llama el job diario, no un endpoint: genera los movimientos de todos los
 * recurrentes activos con cargos pendientes hasta {@code hoy}, de todos los
 * usuarios. Devuelve cuantos movimientos ha creado.
 */
public interface GenerarCargosRecurrentesInterface {
    int generar(LocalDate hoy);
}
