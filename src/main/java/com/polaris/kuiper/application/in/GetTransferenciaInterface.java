package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Transferencia;

public interface GetTransferenciaInterface {
    Transferencia get(Long usuarioId, Long id);
}
