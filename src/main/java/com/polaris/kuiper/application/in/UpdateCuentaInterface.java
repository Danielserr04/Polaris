package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Cuenta;

public interface UpdateCuentaInterface {
    Cuenta update(Long usuarioId, Long id, Cuenta cuenta);
}
