package com.polaris.kuiper.application.in;

import com.polaris.shared.logro.Logro;

import java.time.LocalDate;
import java.util.List;

public interface ListLogrosInterface {

    /**
     * Todos los logros del catalogo con el progreso del usuario, en el orden
     * del catalogo. {@code hoy} decide que meses estan cerrados.
     */
    List<Logro> list(Long usuarioId, LocalDate hoy);
}
