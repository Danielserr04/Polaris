package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.Titulo;

public interface UpdateTituloInterface {
    Titulo update(Long id, Titulo titulo);
}
