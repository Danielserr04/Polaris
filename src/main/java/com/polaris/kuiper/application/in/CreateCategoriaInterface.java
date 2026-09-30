package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Categoria;

public interface CreateCategoriaInterface {
    Categoria create(Long usuarioId, Categoria categoria);
}
