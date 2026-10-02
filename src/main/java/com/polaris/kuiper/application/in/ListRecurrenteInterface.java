package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;

import java.util.List;

public interface ListRecurrenteInterface {
    List<Recurrente> list(Long usuarioId, RecurrenteFilter filter);
}
