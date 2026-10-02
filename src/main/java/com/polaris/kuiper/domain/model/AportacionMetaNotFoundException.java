package com.polaris.kuiper.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien cuando la aportacion existe pero es de otra meta
 * o de otro usuario.
 */
public class AportacionMetaNotFoundException extends NotFoundException {

    public AportacionMetaNotFoundException(Long id) {
        super("Aportacion no encontrada: " + id);
    }
}
