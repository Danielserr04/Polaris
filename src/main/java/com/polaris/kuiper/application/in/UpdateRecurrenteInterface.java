package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Recurrente;

public interface UpdateRecurrenteInterface {
    Recurrente update(Long usuarioId, Long id, Recurrente recurrente);
}
