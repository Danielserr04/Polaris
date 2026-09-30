package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Categoria;

public interface UpdateCategoriaInterface {
    Categoria update(Long usuarioId, Long id, Categoria categoria);
}
