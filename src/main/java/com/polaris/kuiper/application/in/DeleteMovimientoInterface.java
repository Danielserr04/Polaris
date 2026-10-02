package com.polaris.kuiper.application.in;

/** Manda el movimiento a la papelera; no lo borra de verdad. */
public interface DeleteMovimientoInterface {
    void delete(Long usuarioId, Long id);
}
