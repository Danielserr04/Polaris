package com.polaris.kuiper.application.in;

/** Borrado fisico de un movimiento que ya esta en la papelera. */
public interface DeleteDefinitivoMovimientoInterface {
    void deleteDefinitivo(Long usuarioId, Long id);
}
