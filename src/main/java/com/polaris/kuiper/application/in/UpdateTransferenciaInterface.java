package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Transferencia;

public interface UpdateTransferenciaInterface {
    Transferencia update(Long usuarioId, Long id, Transferencia transferencia);
}
