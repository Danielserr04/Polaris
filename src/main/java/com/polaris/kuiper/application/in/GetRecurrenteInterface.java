package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Recurrente;

public interface GetRecurrenteInterface {
    Recurrente get(Long usuarioId, Long id);
}
