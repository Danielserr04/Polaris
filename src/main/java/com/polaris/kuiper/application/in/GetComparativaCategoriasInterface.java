package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.ComparativaCategorias;

import java.time.LocalDate;

public interface GetComparativaCategoriasInterface {
    ComparativaCategorias get(Long usuarioId, LocalDate desde, LocalDate hasta);
}
