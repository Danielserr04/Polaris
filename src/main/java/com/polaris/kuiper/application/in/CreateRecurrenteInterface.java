package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Recurrente;

public interface CreateRecurrenteInterface {
    Recurrente create(Long usuarioId, Recurrente recurrente);
}
