package com.polaris.kuiper.application.in;

import java.util.List;

/** Manda varios movimientos a la papelera: todos o ninguno. */
public interface BorrarMovimientosInterface {
    void borrar(Long usuarioId, List<Long> ids);
}
