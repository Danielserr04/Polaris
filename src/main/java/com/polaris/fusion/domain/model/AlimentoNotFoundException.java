package com.polaris.fusion.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404.
 */
public class AlimentoNotFoundException extends NotFoundException {

    public AlimentoNotFoundException(Long id) {
        super("Alimento no encontrado: " + id);
    }
}
