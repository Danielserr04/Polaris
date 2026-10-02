package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Cuenta;

public interface GetCuentaInterface {
    Cuenta get(Long usuarioId, Long id);
}
