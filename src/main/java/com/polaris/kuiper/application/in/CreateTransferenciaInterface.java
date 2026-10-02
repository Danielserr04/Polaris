package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Transferencia;

public interface CreateTransferenciaInterface {
    Transferencia create(Long usuarioId, Transferencia transferencia);
}
