package com.polaris.fusion.application.in;

import com.polaris.shared.logro.Logro;

import java.util.List;

public interface ListLogrosInterface {

    /** Todos los logros del catalogo con el progreso del usuario, en el orden del catalogo. */
    List<Logro> list(Long usuarioId);
}
