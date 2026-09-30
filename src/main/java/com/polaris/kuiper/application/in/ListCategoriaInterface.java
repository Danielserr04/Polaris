package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaFilter;

import java.util.List;

public interface ListCategoriaInterface {
    List<Categoria> list(Long usuarioId, CategoriaFilter filter);
}
