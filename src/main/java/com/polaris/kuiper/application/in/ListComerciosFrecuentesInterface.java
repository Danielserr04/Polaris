package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.ComercioFrecuente;

import java.time.LocalDate;
import java.util.List;

public interface ListComerciosFrecuentesInterface {
    List<ComercioFrecuente> list(Long usuarioId, LocalDate desde, LocalDate hasta, int limite);
}
