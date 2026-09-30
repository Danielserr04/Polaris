package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Categoria;

public interface GetCategoriaInterface {
    Categoria get(Long usuarioId, Long id);
}
