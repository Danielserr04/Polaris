package com.polaris.kuiper.application.in;

import java.time.LocalDateTime;

/**
 * Lo llama el job diario, no un endpoint: borra de verdad, de todos los
 * usuarios, los movimientos que llevan mas de 30 dias en la papelera.
 * Devuelve cuantos ha borrado.
 */
public interface PurgarPapeleraMovimientoInterface {
    int purgar(LocalDateTime ahora);
}
