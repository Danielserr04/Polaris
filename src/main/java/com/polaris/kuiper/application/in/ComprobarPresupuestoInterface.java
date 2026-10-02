package com.polaris.kuiper.application.in;

import java.time.LocalDate;

/**
 * Lo llama MovimientoService despues de guardar un gasto: si el presupuesto
 * mensual de esa categoria pasa del umbral de aviso o del limite en el mes en
 * curso, crea la notificacion. Un gasto con fecha de otro mes no avisa.
 * Nunca lanza: un aviso no puede tumbar el guardado del movimiento.
 */
public interface ComprobarPresupuestoInterface {
    void comprobar(Long usuarioId, Long categoriaId, LocalDate fecha);
}
