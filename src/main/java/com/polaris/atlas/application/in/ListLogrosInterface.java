package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Logro;

import java.util.List;

public interface ListLogrosInterface {

    /** Todos los logros del catalogo con el progreso del usuario, en el orden del catalogo. */
    List<Logro> list(Long usuarioId);
}
