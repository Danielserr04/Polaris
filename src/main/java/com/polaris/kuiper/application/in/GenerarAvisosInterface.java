package com.polaris.kuiper.application.in;

import java.time.LocalDate;

/**
 * Lo llama el job diario de avisos, no un endpoint: cargos proximos,
 * presupuestos del mes y, el dia 1, el resumen del mes anterior, de todos
 * los usuarios. Devuelve cuantas notificaciones nuevas ha creado.
 */
public interface GenerarAvisosInterface {
    int generar(LocalDate hoy);
}
