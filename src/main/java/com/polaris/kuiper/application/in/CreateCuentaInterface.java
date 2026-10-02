package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Cuenta;

public interface CreateCuentaInterface {
    Cuenta create(Long usuarioId, Cuenta cuenta);
}
